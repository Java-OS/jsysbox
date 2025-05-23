package ir.moke.jsysbox.firewall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.JniNativeLoader;
import ir.moke.jsysbox.JsonUtils;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.model.Set;
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.Statement;
import ir.moke.jsysbox.firewall.statement.VerdictStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JFirewall {
    private static final Logger logger = LoggerFactory.getLogger(JFirewall.class);
    private static final String VALID_PATTERN = "^[a-zA-Z][a-zA-Z0-9._/-]*$";
    private static final Pattern pattern = Pattern.compile(VALID_PATTERN);

    static {
        JniNativeLoader.load("jfirewall");
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

    public static NFTables exportToNFTables() {
        String json = export();
        return JsonUtils.toObject(json, NFTables.class);
    }

    public static void apply(NFTables nfTables) {
        nfTables.getTables().forEach(JFirewall::tableAdd);
        nfTables.getChains().forEach(JFirewall::chainAdd);
        nfTables.getSets().forEach(JFirewall::setAdd);
        nfTables.getRules().forEach(JFirewall::ruleAdd);
    }

    public static void backup(File file) {
        String str = export();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static String serializeJson(NFTables nfTables) {
        return JsonUtils.toJson(nfTables);
    }

    public static NFTables deserializeJson(String json) {
        return JsonUtils.toObject(json, NFTables.class);
    }

    public static byte[] serializeByteCode(NFTables nfTables) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(nfTables);
            os.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new JSysboxException(e);
        }
    }

    public static NFTables deserializeByteCode(byte[] bytes) {
        try (ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (NFTables) os.readObject();
        } catch (Exception e) {
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

    public static NFTables nfTables() {
        String json = export();
        return JsonUtils.toObject(json, NFTables.class);
    }

    public static MetaInfo metaInfo() {
        String json = export();
        JsonNode root = JsonUtils.toObject(json, JsonNode.class);
        ArrayNode nftablesArray = (ArrayNode) root.get("nftables");
        for (JsonNode jsonNode : nftablesArray) {
            if (jsonNode.has("metainfo")) {
                return JsonUtils.toObject(jsonNode.get("metainfo").toString(), MetaInfo.class);
            }
        }
        return null;
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
        return table(name, type);
    }

    public static Table tableAdd(Table table) {
        checkCharacters(table.getName());
        exec("add table %s %s".formatted(table.getType().getValue(), table.getName()));
        return table(table.getName(), table.getType());
    }

    /**
     * fetch list current tables
     *
     * @return List of tables {@link Table}
     */
    public static List<Table> tableList() {
        String result = exec("list tables");
        List<Table> tables = new ArrayList<>();
        JsonNode jsonNode = JsonUtils.toObject(result, JsonNode.class);
        JsonNode nftablesNode = jsonNode.get("nftables");
        for (JsonNode node : nftablesNode) {
            if (node.has("table")) {
                Table table = JsonUtils.toObject(node.get("table").toString(), Table.class);
                tables.add(table);
            }
        }
        return tables;
    }

    /**
     * Rename table
     *
     * @param handle  target table handle
     * @param newName table new name
     */
    public static synchronized void tableRename(int handle, String newName) {
        Table table = table(handle);
        List<List<Rule>> chainRules = chainList(table).stream().map(JFirewall::ruleList).toList();

        tableRemove(handle);
        Table newTable = tableAdd(newName, table.getType());
        for (List<Rule> ruleList : chainRules) {
            for (Rule rule : ruleList) {
                Chain chain = rule.getChain();
                chain.setTable(newTable);
                Chain newChain = chainAdd(chain);
                ruleAdd(newChain, rule.getExpressions(), rule.getStatements(), rule.getComment());
            }
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
     * get specific table by id
     *
     * @param handle table handle id
     * @return table {@link Table}
     */
    public static Table table(int handle) {
        return tableList()
                .stream()
                .filter(item -> item.getHandle() == handle)
                .findFirst()
                .orElse(null);
    }

    /**
     * get specific table by name
     *
     * @param name name of table
     * @return table {@link Table}
     */
    public static Table table(String name, TableType type) {
        return tableList()
                .stream()
                .filter(item -> item.getName().equals(name))
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
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
    public static Chain chainAdd(Table table, String name, ChainType type, ChainHook hook, ChainPolicy policy, Integer priority) throws JSysboxException {
        priority = calculatePriority(table, priority);
        String cmd = "add chain %s %s %s {type %s hook %s priority %s ; policy %s ; }";

        String tableType = table.getType().getValue();
        String tableName = table.getName();
        String chainType = type.getValue();
        String chainHook = hook.getValue();
        String chainPolicy = type.equals(ChainType.NAT) ? ChainPolicy.ACCEPT.getValue() : policy.getValue();
        exec(cmd.formatted(tableType, tableName, name, chainType, chainHook, priority, chainPolicy));

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

    public static Chain chainAdd(int tableHandle, String name) throws JSysboxException {
        Table table = table(tableHandle);
        if (table == null) throw new JSysboxException("Table with handle %s does not exists".formatted(tableHandle));

        checkCharacters(name);
        String cmd = "add chain %s %s %s";
        exec(cmd.formatted(table.getType().getValue(), table.getName(), name));
        return chain(table, name);
    }

    public static Chain chainAdd(Chain chain) throws JSysboxException {
        if (chain.getType() != null) {
            return chainAdd(chain.getTable(), chain.getName(), chain.getType(), chain.getHook(), chain.getPolicy(), chain.getPriority());
        } else {
            return chainAdd(chain.getTable(), chain.getName());
        }
    }

    /**
     * fetch list current chains
     *
     * @return list of chains {@link Chain}
     */
    public static List<Chain> chainList() {
        String result = exec("list chains");
        List<Chain> chains = new ArrayList<>();
        JsonNode jsonNode = JsonUtils.toObject(result, JsonNode.class);
        JsonNode nftablesNode = jsonNode.get("nftables");
        for (JsonNode node : nftablesNode) {
            if (node.has("chain")) {
                Chain chain = JsonUtils.toObject(node.get("chain").toString(), Chain.class);
                chains.add(chain);
            }
        }
        return chains;
    }

    public static List<Chain> chainList(Table table) {
        return chainList().stream().filter(item -> item.getTable().equals(table)).toList();
    }

    public static List<Chain> chainList(int tableHandle) {
        return chainList().stream().filter(item -> item.getTable().getHandle() == tableHandle).toList();
    }

    public static Chain chain(Table table, String name) {
        return chainList().stream()
                .filter(item -> item.getTable().equals(table))
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Chain chain(int tableHandle, int chainHandle) {
        return chainList().stream()
                .filter(item -> item.getTable().getHandle() == tableHandle)
                .filter(item -> item.getHandle() == chainHandle)
                .findFirst()
                .orElse(null);
    }

    public static Chain chain(Table table, int handle) {
        return chainList().stream()
                .filter(item -> item.getTable().equals(table))
                .filter(item -> item.getHandle() == handle)
                .findFirst()
                .orElse(null);
    }

    public static Chain chain(String tableName, TableType tableType, String name) {
        return chainList().stream()
                .filter(item -> item.getTable().getName().equals(tableName))
                .filter(item -> item.getTable().getType().equals(tableType))
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update chain properties
     *
     * @param chain instance of {@link Chain}
     * @param name  new chain name
     */
    public static synchronized void chainUpdate(Chain chain, String name, ChainType type, ChainPolicy policy, ChainHook hook, Integer priority) {
        List<Rule> rules = ruleList(chain);

        // remove old chain
        chainRemove(chain);

        // set new name and apply on firewall
        Optional.ofNullable(name).ifPresent(chain::setName);
        Optional.ofNullable(policy).ifPresent(chain::setPolicy);
        Optional.ofNullable(hook).ifPresent(chain::setHook);
        Optional.ofNullable(type).ifPresent(chain::setType);
        Optional.ofNullable(priority).ifPresent(chain::setPriority);

        Chain newChain = chainAdd(chain);
        for (Rule rule : rules) {
            ruleAdd(newChain, rule.getExpressions(), rule.getStatements(), rule.getComment());
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
        String cmd = "delete chain %s %s handle %s".formatted(tableType.getValue(), tableName, handle);
        exec(cmd);
    }

    /**
     * Update chain priority
     *
     * @param tableHandle  table of chain
     * @param chainHandles List of new chain handles order
     */
    public static synchronized List<Chain> chainSwitch(int tableHandle, List<Integer> chainHandles) {
        List<Chain> currentChains = chainList(tableHandle);
        Map<Chain, List<Rule>> map = new LinkedHashMap<>();
        for (Integer chainHandle : chainHandles) {
            for (int i = 0; i < currentChains.size(); i++) {
                Chain currentChain = currentChains.get(i);
                if (currentChain.getHandle() == chainHandle) {
                    currentChain.setPriority(i);
                    map.put(currentChain, ruleList(currentChain));
                }
            }
        }

        currentChains.forEach(JFirewall::chainRemove);
        for (Chain chain : map.keySet()) {
            chainAdd(chain);
            map.get(chain).forEach(JFirewall::ruleAdd);
        }
        return chainList(tableHandle);
    }

    private static Integer calculatePriority(Table table, Integer priority) {
        return chainList(table).stream()
                .mapToInt(Chain::getPriority)
                .max()
                .orElse(priority);
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
     */
    public static Set setAdd(TableType tableType, String tableName, String setName, SetType setType, List<FlagType> flags, Integer timeout, Integer gcInterval, Integer size, String comment, SetPolicy policy) {
        StringBuilder sb = new StringBuilder("add set %s %s %s { type %s".formatted(tableType.getValue(), tableName, setName, setType.getValue())).append(";");
        if (flags != null && !flags.isEmpty()) {
            sb.append(" flags ").append(String.join(",", flags.stream().map(FlagType::getValue).toList())).append(";");
        }
        Optional.ofNullable(timeout).ifPresent(item -> sb.append(" timeout ").append(item).append("s").append(";"));
        Optional.ofNullable(gcInterval).ifPresent(item -> sb.append(" gc-interval ").append(item).append("s").append(";"));
        Optional.ofNullable(size).ifPresent(item -> sb.append(" size ").append(item).append(";"));
        Optional.ofNullable(comment).ifPresent(item -> sb.append(" comment ").append("\"").append(item).append("\"").append(";"));
        Optional.ofNullable(policy).ifPresent(item -> sb.append(" policy ").append(policy.getValue()).append(";"));
        if (flags != null && flags.contains(FlagType.INTERVAL)) sb.append(" auto-merge ").append(";");
        sb.append(" } ");
        exec(sb.toString());
        return set(tableName, tableType, setName);
    }

    /**
     * find a set
     *
     * @param tableType type of table
     * @param tableName table name
     * @param setName   set name
     * @return the set {@link Set}
     */
    public static Set set(String tableName, TableType tableType, String setName) {
        return setList().stream()
                .filter(item -> item.getTable().getName().equals(tableName))
                .filter(item -> item.getTable().getType().equals(tableType))
                .filter(item -> item.getName().equals(setName))
                .findFirst()
                .orElse(null);
    }

    public static Set set(Table table, String setName) {
        return set(table.getName(), table.getType(), setName);
    }

    public static Set set(Table table, int handle) {
        return setList().stream()
                .filter(item -> item.getTable().equals(table))
                .filter(item -> item.getHandle() == handle)
                .findFirst().orElse(null);
    }

    public static List<Set> set(Table table) {
        return setList().stream()
                .filter(item -> item.getTable().equals(table))
                .toList();
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
     */
    public static void setAdd(Table table, String setName, SetType setType, List<FlagType> flags, Integer timeout, Integer gcInterval, Integer size, String comment, SetPolicy policy) {
        setAdd(table.getType(), table.getName(), setName, setType, flags, timeout, gcInterval, size, comment, policy);
    }

    public static void setAdd(Set set) {
        setAdd(set.getTable(), set.getName(), set.getType(), set.getFlags(), set.getTimeout(), set.getGcInterval(), set.getSize(), set.getComment(), set.getPolicy());
    }

    /**
     * nftables list of current sets
     *
     * @return list of {@link Set}
     */
    public static List<Set> setList() {
        String result = exec("list sets");
        List<Set> sets = new ArrayList<>();
        JsonNode jsonNode = JsonUtils.toObject(result, JsonNode.class);
        JsonNode nftablesNode = jsonNode.get("nftables");
        for (JsonNode node : nftablesNode) {
            if (node.has("set")) {
                Set set = JsonUtils.toObject(node.get("set").toString(), Set.class);
                sets.add(set);
            }
        }
        return sets;
    }

    public static synchronized void setRename(int tableHandle, int setHandle, String newName) {
        Table table = table(tableHandle);
        Set set = set(table, setHandle);
        //TODO: Implement me
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
    public static void ruleAdd(Chain chain, List<Expression> expressions, List<Statement> statements, String comment) {
        try {
            Table table = chain.getTable();
            String chainName = chain.getName();
            TableType tableType = table.getType();
            String tableName = table.getName();
            String expr = expressions != null && !expressions.isEmpty() ? String.join(" ", expressions.stream().map(Expression::toString).toList()) : "";
            String stt = statements != null && !statements.isEmpty() ? String.join(" ", statements.stream().sorted(sortStatements()).map(Statement::toString).toList()) : "";

            StringBuilder sb = new StringBuilder("add rule");
            sb.append(" ").append(tableType.getValue());
            sb.append(" ").append(tableName);
            sb.append(" ").append(chainName);
            sb.append(" ").append(expr);
            sb.append(" ").append(stt);
            Optional.ofNullable(comment).ifPresent(item -> sb.append(" comment ").append("\"").append(item).append("\""));
            exec(sb.toString());
        } catch (Exception e) {
            if (e instanceof JSysboxException jse) {
                logger.error("nftables syntax error", jse);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static void ruleAdd(Rule rule) {
        ruleAdd(rule.getChain(), rule.getExpressions(), rule.getStatements(), rule.getComment());
    }

    /**
     * Add rule from json of {@link Rule} structure
     *
     * @param json json structure
     */
    public static void ruleAdd(String json) {
        Rule rule = JsonUtils.toObject(json, Rule.class);
        ruleAdd(rule.getChain(), rule.getExpressions(), rule.getStatements(), rule.getComment());
    }

    public static void ruleInsert(Chain chain, List<Expression> expressions, List<Statement> statements, String comment, int handle) {
        try {
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
                    " " + String.join(" ", statements.stream().sorted(sortStatements()).map(Statement::toString).toList()) +
                    " comment " + "\"" + comment + "\"";
            exec(sb);
        } catch (Exception e) {
            if (e instanceof JSysboxException jse) {
                logger.error("nftables syntax error", jse);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return List of {@link Rule}
     */
    public static List<Rule> ruleList(Chain chain) {
        String result = exec(chain != null ? "list chain %s %s %s".formatted(chain.getTable().getType().getValue(), chain.getTable().getName(), chain.getName()) : "list ruleset");
        List<Rule> rules = new ArrayList<>();
        JsonNode jsonNode = JsonUtils.toObject(result, JsonNode.class);
        JsonNode nftablesNode = jsonNode.get("nftables");
        for (JsonNode node : nftablesNode) {
            if (node.has("rule")) {
                JsonNode ruleNode = node.get("rule");
                Rule rule = JsonUtils.toObject(ruleNode.toString(), Rule.class);
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * Check rule exists
     *
     * @param id rule handle id
     */
    public static void ruleCheckExists(Chain chain, long id) {
        if (chain == null) throw new JSysboxException("Empty chain");
        boolean exists = ruleList(chain)
                .stream()
                .filter(item -> item.getChain().getTable().equals(chain.getTable()))
                .filter(item -> item.getChain().equals(chain))
                .anyMatch(item -> item.getHandle() == id);
        if (!exists) throw new JSysboxException("rule with handle %s does not exists".formatted(id));
    }

    public static Rule rule(Chain chain, int handle) {
        return ruleList(chain)
                .stream()
                .filter(item -> item.getChain().equals(chain))
                .filter(item -> item.getHandle() == handle)
                .findFirst()
                .orElse(null);
    }

    /**
     * Update rule
     */
    public static synchronized void ruleUpdate(Chain chain, int handle, List<Expression> expressions, List<Statement> statements, String comment) {
        Rule rule = rule(chain, handle);
        rule.setExpressions(expressions);
        rule.setStatements(statements);
        rule.setComment(comment);
        ruleUpdate(rule);
    }

    public static synchronized void ruleUpdate(Rule rule) {
        List<Rule> ruleList = ruleList(rule.getChain());
        ruleList.forEach(JFirewall::ruleRemove);

        int index = ruleList.indexOf(rule);
        if (index != -1) ruleList.set(index, rule);
        ruleList.forEach(JFirewall::ruleAdd);
    }

    /**
     * Change priority of rule r1 and r2 .
     *
     * @param chain       Target chain
     * @param ruleHandles new order list of rule handles
     */
    public static synchronized List<Rule> ruleSwitch(Chain chain, List<Integer> ruleHandles) {
        List<Rule> rules = ruleList(chain);
        rules.forEach(JFirewall::ruleRemove);

        for (Integer handle : ruleHandles) {
            rules.stream().filter(item -> item.getHandle().equals(handle)).findFirst().ifPresent(JFirewall::ruleAdd);
        }
        return ruleList(chain);
    }

    /**
     * @param chain    nftables {@link Chain}
     * @param handleId rule handle handleId
     */
    public static void ruleRemove(Chain chain, long handleId) {
        ruleCheckExists(chain, handleId);
        String tableName = chain.getTable().getName();
        TableType tableType = chain.getTable().getType();
        String chainName = chain.getName();
        String cmd = "delete rule %s %s %s handle %s".formatted(tableType.getValue(), tableName, chainName, handleId);
        exec(cmd);
    }

    public static void ruleRemove(Rule rule) {
        ruleRemove(rule.getChain(), rule.getHandle());
    }

    private static Comparator<Statement> sortStatements() {
        return (stt1, stt2) -> {
            if (stt1 instanceof VerdictStatement && !(stt2 instanceof VerdictStatement)) {
                return 1;
            } else if (!(stt1 instanceof VerdictStatement) && stt2 instanceof VerdictStatement) {
                return -1;
            } else {
                return 0;
            }
        };
    }
}
