package ir.moke.jsysbox.firewall;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.Statement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
        if (!matches) throw new JSysboxException("String contains invalid character: " + str);
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
            throw new JSysboxException(e);
        }
    }

    /**
     * Rename table
     *
     * @param handle  target table handle
     * @param newName table new name
     */
    public static synchronized void tableRename(int handle, String newName) {
        Table table = table(handle);
        String oldName = table.getName();
        String type = table.getType().getValue();
        try {
            String json = export();
            JsonNode root = om.readTree(json);
            ArrayNode nftablesArray = (ArrayNode) root.get("nftables");
            for (JsonNode node : nftablesArray) {
                if (node.has("table")) {
                    ObjectNode tableNode = (ObjectNode) node.get("table");
                    // Match both table name and family
                    if (oldName.equals(tableNode.get("name").asText()) && type.equals(tableNode.get("family").asText())) {
                        tableNode.put("name", newName);
                    }
                } else if (node.has("chain")) {
                    ObjectNode chainNode = (ObjectNode) node.get("chain");
                    // Match chains referencing the table by both name and family
                    if (oldName.equals(chainNode.get("table").asText()) && type.equals(chainNode.get("family").asText())) {
                        chainNode.put("table", newName);
                    }
                } else if (node.has("set")) {
                    ObjectNode setNode = (ObjectNode) node.get("set");
                    // Match chains referencing the table by both name and family
                    if (oldName.equals(setNode.get("table").asText()) && type.equals(setNode.get("family").asText())) {
                        setNode.put("table", newName);
                    }
                }
            }

            String path = "/tmp/jfirewall.rules";
            File file = new File(path);
            om.writeValue(file, root);
            restore(path);
        } catch (Exception e) {
            throw new JSysboxException(e);
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
     * nftables remove table
     *
     * @param table {@link Table}
     */
    public static void tableRemove(Table table) {
        tableCheckExists(table.getName());
        exec("delete table handle %s".formatted(table.getHandle()));
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
     * Check table with specific name exists
     *
     * @param name table name
     */
    public static void tableCheckExists(String name) {
        boolean exists = tableList().stream().anyMatch(item -> item.getName().equals(name));
        if (!exists) throw new JSysboxException("Table with name %s does not exists".formatted(name));
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
        checkCharacters(name);
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
            throw new JSysboxException(e);
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
     * Rename chain
     *
     * @param handle  chain handle id
     * @param newName new chain name
     */
    public static synchronized void chainRename(int handle, String newName) {
        Chain chain = chain(handle);
        String oldName = chain.getName();
        Table table = chain.getTable();
        String tableName = table.getName();
        String type = table.getType().getValue();

        try {
            String json = export();
            JsonNode root = om.readTree(json);
            ArrayNode nftablesArray = (ArrayNode) root.get("nftables");
            for (JsonNode node : nftablesArray) {
                if (node.has("chain")) {
                    ObjectNode chainNode = (ObjectNode) node.get("chain");
                    if (handle == chainNode.get("handle").asInt() && oldName.equals(chainNode.get("name").asText()) && tableName.equals(chainNode.get("table").asText()) && type.equals(chainNode.get("family").asText())) {
                        chainNode.put("name", newName);
                    }
                }
            }

            String path = "/tmp/jfirewall.rules";
            File file = new File(path);
            om.writeValue(file, root);
            restore(path);
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    /**
     * nftables remove chain
     *
     * @param chain chain object to remove {@link Chain}
     */
    public static void chainRemove(Chain chain) {
        int handle = chain.getHandle();
        TableType tableType = chain.getTable().getType();
        String tableName = chain.getTable().getName();
        chainCheckExists(handle);
        String cmd = "delete chain %s %s handle %s".formatted(tableType.getValue(), tableName, handle);
        exec(cmd);
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
    public static Set setAdd(TableType tableType, String tableName, String setName, SetType setType, List<FlagType> flags, Integer timeout, Integer gcInterval, Integer size, String comment, SetPolicy policy, boolean autoMerge) {
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

        return set(tableType, tableName, setName);
    }

    /**
     * find a set
     *
     * @param tableType type of table
     * @param tableName table name
     * @param setName   set name
     * @return the set {@link Set}
     */
    public static Set set(TableType tableType, String tableName, String setName) {
        return setList().stream()
                .filter(item -> item.getTable().getName().equals(tableName))
                .filter(item -> item.getTable().getType().equals(tableType))
                .filter(item -> item.getName().equals(setName))
                .findFirst()
                .orElse(null);
    }

    public static Set set(Table table, String setName) {
        return setList().stream()
                .filter(item -> item.getTable().getName().equals(table.getName()))
                .filter(item -> item.getTable().getType().equals(table.getType()))
                .filter(item -> item.getName().equals(setName))
                .findFirst()
                .orElse(null);
    }

    /**
     * nftables add new set
     *
     * @param table      table {@link Table}
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
    public static void setAdd(Table table, String setName, SetType setType, List<FlagType> flags, Integer timeout, Integer gcInterval, Integer size, String comment, SetPolicy policy, boolean autoMerge) {
        setAdd(table.getType(), table.getName(), setName, setType, flags, timeout, gcInterval, size, comment, policy, autoMerge);
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
            throw new JSysboxException(e);
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
     * rename set
     *
     * @param handle  set handle id
     * @param newName set new name
     */
    public static synchronized void setRename(int handle, String newName) {
        Set set = set(handle);
        String oldName = set.getName();
        Table table = set.getTable();
        String tableName = table.getName();
        String type = table.getType().getValue();

        try {
            String json = export();
            JsonNode root = om.readTree(json);
            ArrayNode nftablesArray = (ArrayNode) root.get("nftables");
            for (JsonNode node : nftablesArray) {
                if (node.has("set")) {
                    ObjectNode setNode = (ObjectNode) node.get("set");
                    if (oldName.equals(setNode.get("name").asText()) && tableName.equals(setNode.get("table").asText()) && type.equals(setNode.get("family").asText())) {
                        setNode.put("name", newName);
                    }
                }
            }

            String path = "/tmp/jfirewall.rules";
            File file = new File(path);
            om.writeValue(file, root);
            restore(path);
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
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
     * {add | insert} rule [family] table chain [handle | index] statement ... [comment comment]
     * example :
     * add rule filter output ip daddr 192.168.0.0/24 accept
     */
    public static void ruleAdd(Chain chain, List<Expression> expressions, Statement statement, String comment) {
        chainCheckExists(chain.getHandle());
        Table table = chain.getTable();
        String chainName = chain.getName();
        TableType tableType = table.getType();
        String tableName = table.getName();
        String sb = "add rule" +
                " " + tableType.getValue() +
                " " + tableName +
                " " + chainName +
                " " + String.join(" ", expressions.stream().map(Expression::toString).toList()) +
                " " + statement +
                " comment " + "\"" + comment + "\"";
        exec(sb);
    }

    public static void ruleInsert(Chain chain, List<Expression> expressions, Statement statement, String comment, int handle) {
        chainCheckExists(chain.getHandle());
        Table table = chain.getTable();
        String chainName = chain.getName();
        TableType tableType = table.getType();
        String tableName = table.getName();
        String sb = "insert rule" +
                " " + tableType.getValue() +
                " " + tableName +
                " " + chainName +
                " position " + handle +
                " " + String.join(" ", expressions.stream().map(Expression::toString).toList()) +
                " " + statement +
                " comment " + "\"" + comment + "\"";
        exec(sb);
    }

    /**
     * Update current rule
     * old rule removed and new rule inserted
     */
    public static void ruleUpdate(Chain chain, List<Expression> expressions, Statement statement, String comment, Integer handle) {
        if (handle == null) {
            ruleAdd(chain, expressions, statement, comment);
        } else {
            ruleInsert(chain, expressions, statement, comment, handle);
        }
    }

    /**
     * @return List of {@link Rule}
     */
    public static List<Rule> ruleList() {
        String result = exec("list ruleset");
        try {
            List<Rule> rules = new ArrayList<>();
            JsonNode jsonNode = om.readValue(result, JsonNode.class);
            JsonNode nftablesNode = jsonNode.get("nftables");
            for (JsonNode node : nftablesNode) {
                if (node.has("rule")) {
                    JsonNode ruleNode = node.get("rule");
                    Rule rule = om.readValue(ruleNode.toString(), Rule.class);
                    rules.add(rule);
                }
            }
            return rules;
        } catch (JsonProcessingException e) {
            throw new JSysboxException(e);
        }
    }

    /**
     * find rules by chain name
     *
     * @param chain name of {@link Chain}
     * @return list of {@link Rule}
     */
    public static List<Rule> findRulesByChain(Chain chain) {
        return ruleList().stream().filter(item -> item.getChain().equals(chain)).toList();
    }

    /**
     * Check rule exists
     *
     * @param id rule handle id
     */
    public static void ruleCheckExists(long id) {
        boolean exists = ruleList().stream().anyMatch(item -> item.getHandle() == id);
        if (!exists) throw new JSysboxException("rule with handle %s does not exists".formatted(id));
    }

    public static Rule rule(int handle) {
        Rule rule = ruleList().stream().filter(item -> item.getHandle() == handle).findFirst().orElse(null);
        if (rule == null) throw new JSysboxException("Rule with handle %s does not exists".formatted(handle));
        return rule;
    }

    /**
     * Update rule comment
     *
     * @param handle  set handle id
     * @param comment rule comment
     */
    public static void ruleComment(int handle, String comment) {
        Rule rule = rule(handle);
        Chain chain = rule.getChain();
        Table table = chain.getTable();
        String chainName = chain.getName();
        String tableName = table.getName();
        String type = table.getType().getValue();

        try {
            String json = export();
            JsonNode root = om.readTree(json);
            ArrayNode nftablesArray = (ArrayNode) root.get("nftables");
            for (JsonNode node : nftablesArray) {
                if (node.has("rule")) {
                    ObjectNode ruleNode = (ObjectNode) node.get("rule");
                    if (handle == ruleNode.get("handle").asInt() && chainName.equals(ruleNode.get("chain").asText()) && tableName.equals(ruleNode.get("table").asText()) && type.equals(ruleNode.get("family").asText())) {
                        ruleNode.put("comment", comment);
                    }
                }
            }

            String path = "/tmp/jfirewall.rules";
            File file = new File(path);
            om.writeValue(file, root);
            restore(path);
            Files.delete(file.toPath());
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }


    /**
     * @param chain nftables {@link Chain}
     * @param id    rule handle id
     */
    public static void ruleRemove(Chain chain, long id) {
        ruleCheckExists(id);
        exec("delete rule %s %s handle %s".formatted(chain.getTable().getName(), chain.getName(), id));
    }
}
