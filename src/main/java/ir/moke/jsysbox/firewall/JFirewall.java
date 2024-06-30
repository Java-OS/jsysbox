package ir.moke.jsysbox.firewall;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;
import ir.moke.jsysbox.firewall.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JFirewall {

    private static final String VALID_PATTERN = "^[a-zA-Z][a-zA-Z0-9]*$";
    private static final Pattern pattern = Pattern.compile(VALID_PATTERN);
    private static final ObjectMapper om = new ObjectMapper();

    static {
        JniNativeLoader.load("jfirewall");

        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        om.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, false);
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * restore nftables config from file
     *
     * @param filePath config file path
     */
    public native static void restore(String filePath);

    /**
     * execute nftables commands
     *
     * @param command nftables command
     * @return result of command
     */
    private native static String exec(String command);

    private static void checkCharacters(String str) {
        Matcher matcher = pattern.matcher(str);
        boolean matches = matcher.matches();
        if (!matches) throw new JSysboxException("String contain invalid character: " + str);
    }

    /**
     * flush (erase, delete, wipe) the complete ruleset
     *
     * @param type type of table {@link TableType}
     */
    public static void flush(TableType type) {
        exec("flush ruleset %s".formatted(type != null ? type.getValue() : ""));
    }

    /**
     * nftables add new table
     *
     * @param name table name
     * @param type type of table {@link TableType}
     */
    public static void tableAdd(String name, TableType type) {
        checkCharacters(name);
        exec("add table %s %s".formatted(type.getValue(), name));
    }

    /**
     * fetch list current tables
     *
     * @return List of tables {@link Table}
     */
    public static List<Table> tableList() {
        String result = exec("list tables");
        try {
            List<Table> tables = new ArrayList<>();
            JsonNode jsonNode = om.readValue(result, JsonNode.class);
            JsonNode nftablesNode = jsonNode.get("nftables");
            for (JsonNode node : nftablesNode) {
                if (node.has("table")) {
                    Table table = om.readValue(node.get("table").toString(), Table.class);
                    tables.add(table);
                }
            }

            return tables;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * nftables remove table
     *
     * @param handle table handle id
     */
    public static void tableRemove(long handle) {
        checkTableExists(handle);
        exec("delete table handle %s".formatted(handle));
    }

    /**
     * Check table with specific id exists
     *
     * @param handle table handle id
     */
    public static void checkTableExists(long handle) {
        boolean exists = tableList().stream().anyMatch(item -> item.getHandle() == handle);
        if (!exists) throw new JSysboxException("Table with handle %s does not exists".formatted(handle));
    }

    /**
     * get specific table by id
     *
     * @param handle table handle id
     * @return table {@link Table}
     */
    public static Table getTable(long handle) {
        Table table = tableList().stream().filter(item -> item.getHandle() == handle).findFirst().orElse(null);
        if (table == null) throw new JSysboxException("Table with handle %s does not exists".formatted(handle));
        return table;
    }

    /**
     * nftables add new chain
     * command format :
     * add chain [<family>] <table_name> <chain_name> { type <type> hook <hook> priority <value> \; [policy <policy> \;]
     *
     * @param handle   table handle id
     * @param name     chain name
     * @param type     chain type {@link ChainType}
     * @param hook     chain hook {@link ChainHook}
     * @param policy   chain policy {@link ChainPolicy}
     * @param priority chain priority
     */
    public static void addChain(long handle, String name, ChainType type, ChainHook hook, ChainPolicy policy, int priority) throws JSysboxException {
        Table table = getTable(handle);
        String cmd = "add chain %s %s %s {type %s hook %s priority %s ; policy %s ; }";
        exec(cmd.formatted(table.getFamily().getValue(), table.getName(), name, type.getValue(), hook.getValue(), priority, policy.getValue()));
    }

    /**
     * fetch list current chains
     *
     * @return list of chains {@link Chain}
     */
    public static List<Chain> listChain() {
        String result = exec("list chains");
        try {
            List<Chain> chains = new ArrayList<>();
            JsonNode jsonNode = om.readValue(result, JsonNode.class);
            JsonNode nftablesNode = jsonNode.get("nftables");
            for (JsonNode node : nftablesNode) {
                if (node.has("chain")) {
                    Chain chain = om.readValue(node.get("chain").toString(), Chain.class);
                    chains.add(chain);
                }
            }
            return chains;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * check chain exist by handle id
     *
     * @param handle handle id
     */
    public static void checkChainExists(long handle) {
        boolean exists = listChain().stream().anyMatch(item -> item.getHandle() == handle);
        if (!exists) throw new JSysboxException("Chain with handle %s does not exists".formatted(handle));
    }

    public static Chain getChain(long handle) {
        Chain chain = listChain().stream().filter(item -> item.getHandle() == handle).findFirst().orElse(null);
        if (chain == null) throw new JSysboxException("Chain with handle %s does not exists".formatted(handle));
        return chain;
    }

    /**
     * nftables remove chain
     * command format :
     * by id : delete chain <type> <table name> handle <handle id>
     *
     * @param tableName name of name
     * @param tableType type of table {@link TableType}
     * @param handle    handle id
     */
    public static void removeChain(String tableName, TableType tableType, long handle) {
        checkChainExists(handle);
        String cmd = "delete chain %s %s handle %s";
        exec(cmd.formatted(tableType.getValue(), tableName, handle));
    }
}
