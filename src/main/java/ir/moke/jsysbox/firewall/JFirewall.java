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
import ir.moke.jsysbox.firewall.statement.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JFirewall {

    private static final String VALID_PATTERN = "^[a-zA-Z][a-zA-Z0-9]*$";
    private static final Pattern pattern = Pattern.compile(VALID_PATTERN);
    private static final ObjectMapper om = new ObjectMapper();

    static {
        JniNativeLoader.load("jfirewall");

        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
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

    public static String export() {
        return exec("list ruleset");
    }

    public static void save(File file) {
        String str = export();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

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
    public static Table tableAdd(String name, TableType type) {
        checkCharacters(name);
        exec("add table %s %s".formatted(type.getValue(), name));
        return JFirewall.table(name);
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
    public static void tableRemove(int handle) {
        tableCheckExists(handle);
        exec("delete table handle %s".formatted(handle));
    }

    /**
     * Check table with specific id exists
     *
     * @param handle table handle id
     */
    public static void tableCheckExists(int handle) {
        boolean exists = tableList().stream().anyMatch(item -> item.getHandle() == handle);
        if (!exists) throw new JSysboxException("Table with handle %s does not exists".formatted(handle));
    }

    /**
     * get specific table by id
     *
     * @param handle table handle id
     * @return table {@link Table}
     */
    public static Table table(int handle) {
        Table table = tableList().stream().filter(item -> item.getHandle() == handle).findFirst().orElse(null);
        if (table == null) throw new JSysboxException("Table with handle %s does not exists".formatted(handle));
        return table;
    }

    /**
     * get specific table by name
     *
     * @param name name of table
     * @return table {@link Table}
     */
    public static Table table(String name) {
        Table table = tableList().stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
        if (table == null) throw new JSysboxException("Table with name %s does not exists".formatted(name));
        return table;
    }

    /**
     * nftables add new chain
     * command format :
     * add chain [<family>] <table_name> <chain_name> { type <type> hook <hook> priority <value> \; [policy <policy> \;]
     *
     * @param table    target table
     * @param name     chain name
     * @param type     chain type {@link ChainType}
     * @param hook     chain hook {@link ChainHook}
     * @param policy   chain policy {@link ChainPolicy}
     * @param priority chain priority
     * @return {@link Chain}
     */
    public static Chain chainAdd(Table table, String name, ChainType type, ChainHook hook, ChainPolicy policy, int priority) throws JSysboxException {
        String cmd = "add chain %s %s %s {type %s hook %s priority %s ; policy %s ; }";
        exec(cmd.formatted(table.getType().getValue(), table.getName(), name, type.getValue(), hook.getValue(), priority, policy.getValue()));
        return chain(table, name);
    }

    /**
     * nftables add new chain
     *
     * @param table target table
     * @param name  chain name
     * @return {@link Chain}
     */
    public static Chain chainAdd(Table table, String name) throws JSysboxException {
        String cmd = "add chain %s %s %s";
        exec(cmd.formatted(table.getType().getValue(), table.getName(), name));
        return chain(table, name);
    }

    /**
     * fetch list current chains
     *
     * @return list of chains {@link Chain}
     */
    public static List<Chain> chainList() {
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
    public static void chainCheckExists(int handle) {
        boolean exists = chainList().stream().anyMatch(item -> item.getHandle() == handle);
        if (!exists) throw new JSysboxException("Chain with handle %s does not exists".formatted(handle));
    }

    public static Chain chain(int handle) {
        Chain chain = chainList().stream().filter(item -> item.getHandle() == handle).findFirst().orElse(null);
        if (chain == null) throw new JSysboxException("Chain with handle %s does not exists".formatted(handle));
        return chain;
    }

    public static Chain chain(Table table, String name) {
        Chain chain = chainList().stream()
                .filter(item -> item.getTable().getHandle() == table.getHandle())
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (chain == null) throw new JSysboxException("Chain with table handle %s and name %s does not exists".formatted(table.getHandle(), name));
        return chain;
    }

    /**
     * nftables remove chain
     * command format :
     * by id : delete chain <type> <table name> handle <handle id>
     *
     * @param chain chain object to remove {@link Chain}
     */
    public static void chainRemove(Chain chain) {
        int handle = chain.getHandle();
        TableType tableType = chain.getTable().getType();
        String tableName = chain.getName();
        chainCheckExists(handle);
        String cmd = "delete chain %s %s handle %s";
        exec(cmd.formatted(tableType.getValue(), tableName, handle));
    }

    /**
     * nftables add new set
     *
     * @param tableType  type of table {@link TableType}
     * @param tableName  name of table
     * @param setName    name of set
     * @param setType    type of set {@link SetType}
     * @param flags      list of set flags {@link FlagType}
     * @param timeout    set timeout
     * @param gcInterval set garbage collector timeout
     * @param size       size of elements
     * @param comment    set comment
     * @param policy     set policy type {@link SetPolicy}
     * @param autoMerge  set auto-merge
     */
    public static void setAdd(TableType tableType, String tableName, String setName, SetType setType, List<FlagType> flags, Integer timeout, Integer gcInterval, Integer size, String comment, SetPolicy policy, boolean autoMerge) {
        if (autoMerge && !flags.contains(FlagType.INTERVAL)) throw new JSysboxException("auto-merge only work with interval set");

        StringBuilder sb = new StringBuilder("add set %s %s %s { type %s".formatted(tableType.getValue(), tableName, setName, setType.getValue())).append(";");
        if (flags != null && !flags.isEmpty()) {
            sb.append(" flags ").append(String.join(",", flags.stream().map(FlagType::getValue).toList())).append(";");
        }
        Optional.ofNullable(timeout).ifPresent(item -> sb.append(" timeout ").append(item).append("s").append(";"));
        Optional.ofNullable(gcInterval).ifPresent(item -> sb.append(" gc-interval ").append(item).append("s").append(";"));
        Optional.ofNullable(size).ifPresent(item -> sb.append(" size ").append(item).append(";"));
        Optional.ofNullable(comment).ifPresent(item -> sb.append(" comment ").append("\"").append(item).append("\"").append(";"));
        Optional.ofNullable(policy).ifPresent(item -> sb.append(" policy ").append(policy.getValue()).append(";"));
        if (autoMerge) sb.append(" auto-merge ").append(";");
        sb.append(" } ");

        exec(sb.toString());
    }

    /**
     * nftables list of current sets
     *
     * @return list of {@link Set}
     */
    public static List<Set> setList() {
        String result = exec("list sets");
        try {
            List<Set> sets = new ArrayList<>();
            JsonNode jsonNode = om.readValue(result, JsonNode.class);
            JsonNode nftablesNode = jsonNode.get("nftables");
            for (JsonNode node : nftablesNode) {
                if (node.has("set")) {
                    Set set = om.readValue(node.get("set").toString(), Set.class);
                    sets.add(set);
                }
            }
            return sets;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * nftables get special set
     *
     * @param handle set handle id
     * @return {@link Set}
     */
    public static Set set(int handle) {
        return setList().stream().filter(item -> item.getHandle() == handle).findFirst().orElseThrow(() -> new JSysboxException("Set with handle %s does not exists".formatted(handle)));
    }

    /**
     * check set with special handle id exists
     *
     * @param handle set handle id
     * @throws JSysboxException return exception if it does not exist
     */
    public static void setCheckExists(int handle) throws JSysboxException {
        boolean exists = setList().stream().anyMatch(item -> item.getHandle() == handle);
        if (!exists) throw new JSysboxException("Set with handle %s does not exists".formatted(handle));
    }

    /**
     * nftables remove special set
     *
     * @param set set to remove {@link Set}
     */
    public static void setRemove(Set set) {
        int handle = set.getHandle();
        TableType tableType = set.getTable().getType();
        String tableName = set.getTable().getName();
        setCheckExists(handle);
        String cmd = "delete set %s %s handle %s";
        exec(cmd.formatted(tableType.getValue(), tableName, handle));
    }

    /**
     * replace element of set
     *
     * @param set   target set to add element
     * @param items list of elements
     */
    public static void setAddElement(Set set, List<String> items) {
        int handle = set.getHandle();
        setCheckExists(handle);
        String tableType = set.getTable().getType().getValue();
        String tableName = set.getTable().getName();
        String name = set.getName();
        String cmd = "add element %s %s %s { %s }".formatted(tableType, tableName, name, String.join(",", items));
        exec(cmd);
    }

    /**
     * remove items from set elements
     *
     * @param set   target set to remove element
     * @param items list of elements
     */
    public static void setRemoveElement(Set set, List<String> items) {
        int handle = set.getHandle();
        setCheckExists(handle);
        String tableType = set.getTable().getType().getValue();
        String tableName = set.getTable().getName();
        String name = set.getName();
        String cmd = "delete element %s %s %s { %s }".formatted(tableType, tableName, name, String.join(",", items));
        exec(cmd);
    }

    /**
     * nftables add new rule
     * command format :
     * {add | insert} rule [family] table chain [handle handle | index index] statement ... [comment comment]
     * example :
     * add rule filter output ip daddr 192.168.0.0/24 accept
     */
    public static void ruleAdd(Chain chain, List<Expression> expressions, Statement statement, String comment) {
        chainCheckExists(chain.getHandle());
        Table table = chain.getTable();
        String chainName = chain.getName();
        TableType tableType = table.getType();
        String tableName = table.getName();
        String sb = "add rule" + " " + tableType.getValue() +
                " " + tableName +
                " " + chainName +
                " " + String.join(" ", expressions.stream().map(Expression::toString).toList()) +
                " " + statement +
                " comment " + "\"" + comment + "\"";
        System.out.println(sb);
//        exec(sb);
    }

    public static List<Rule> ruleList() {
        //TODO: Implement me ...
        return null;
    }

    public static void ruleRemove(Set set) {
        //TODO: Implement me ...
    }

    public static void ruleChangePriority(Rule rule,long priority) {
        //TODO: Implement me ...
    }
}
