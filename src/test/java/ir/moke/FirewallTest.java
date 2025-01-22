package ir.moke;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.expression.*;
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FirewallTest {
    private static final Logger logger = LoggerFactory.getLogger(FirewallTest.class);

    @BeforeAll
    public static void init() {
        System.out.println("Execute <Init>");
        JFirewall.flush(null);
    }

    @Test
    @Order(0)
    public void checkTableAdd() {
        logger.info("Execute <checkTableAdd>");
        Table firstTable = JFirewall.tableAdd("FirstTable", TableType.IPv4);
        Assertions.assertEquals("FirstTable", firstTable.getName());
        Assertions.assertEquals(TableType.IPv4, firstTable.getType());

        Table secondTable = JFirewall.tableAdd("SecondTable", TableType.INET);
        Assertions.assertEquals("SecondTable", secondTable.getName());
        Assertions.assertEquals(TableType.INET, secondTable.getType());

        Table thirdTable = JFirewall.tableAdd("ThirdTable", TableType.IPv4);
        Assertions.assertEquals("ThirdTable", thirdTable.getName());
        Assertions.assertEquals(TableType.IPv4, thirdTable.getType());
    }

    @Test
    @Order(1)
    public void checkTableList() {
        logger.info("Execute <checkTableList>");
        List<Table> tables = JFirewall.tableList();
        Assertions.assertEquals(3, tables.size());
    }

    @Test
    @Order(2)
    public void checkTableByName() {
        logger.info("Execute <checkTableByName>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Assertions.assertNotNull(table);
    }

    /**
     * Note1: Duplicate table with same name and type does not have any side effect
     * Example :
     * Note2: Table names is same but type is different
     * table ip FirstTable {
     * }
     * table ip6 FirstTable {
     * }
     */
    @Test
    @Order(3)
    public void checkDuplicateTableName() {
        logger.info("Execute <checkDuplicateTableName>");
        Table table = JFirewall.tableAdd("FirstTable", TableType.IPv4);
        Assertions.assertNotNull(table);
    }

    @Test
    @Order(4)
    public void checkChainAdd() {
        logger.info("Execute <checkChainAdd>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Chain c1Chain = JFirewall.chainAdd(table, "c1", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);
        Assertions.assertNotNull(c1Chain);
        Assertions.assertEquals("c1", c1Chain.getName());
        Assertions.assertEquals(ChainType.FILTER, c1Chain.getType());

        Chain c2Chain = JFirewall.chainAdd(table, "c2");
        Assertions.assertNotNull(c2Chain);
        Assertions.assertEquals("c2", c2Chain.getName());
        Assertions.assertNull(c2Chain.getType());
    }

    @Test
    @Order(5)
    public void checkChainList() {
        logger.info("Execute <checkChainList>");
        List<Chain> chains = JFirewall.chainList();
        Assertions.assertEquals(2, chains.size());
    }

    @Test
    @Order(6)
    public void checkSetAdd() {
        logger.info("Execute <checkSetAdd>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);

        Set set = JFirewall.setAdd(table.getType(), table.getName(), "localNetworkSet", SetType.IPV4_ADDR, List.of(FlagType.INTERVAL), null, null, 30, "My Local Network", SetPolicy.MEMORY);
        Assertions.assertNotNull(set);
        Assertions.assertEquals(set.getTable(), table);
        Assertions.assertEquals("localNetworkSet", set.getName());
    }

    @Test
    @Order(7)
    public void checkSetExists() {
        logger.info("Execute <checkSetExists>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");
        Assertions.assertNotNull(localNetworkSet);
    }

    @Test
    @Order(8)
    public void checkSetAddItem() {
        logger.info("Execute <checkSetAddItem>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");

        List<String> elements = new ArrayList<>();
        elements.add("127.0.0.1");
        elements.add("192.168.1.10");
        elements.add("20.20.20.127");
        JFirewall.setAddElement(localNetworkSet, elements);

        Set localNetworkSetAfterUpdate = JFirewall.set(table, "localNetworkSet");
        Assertions.assertEquals(localNetworkSetAfterUpdate.getElements().size(), elements.size());
    }

    @Test
    @Order(100)
    public void checkRuleAdd() {
        logger.info("Execute <checkRuleAdd>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Chain c1Chain = JFirewall.chainAdd(table, "c1", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);

        List<Expression> expressionList = new ArrayList<>();
        IpExpression ipExpression = new IpExpression(IpExpression.Field.PROTOCOL, Operation.EQ, List.of(Protocols.IP.getValue()));
        TcpExpression tcpExpression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("54"));

        expressionList.add(ipExpression);
        expressionList.add(tcpExpression);

        Statement verdictStatement = new VerdictStatement(VerdictStatement.Type.DROP);
        Statement logStatement = new LogStatement("First log");
        Statement counterStatement = new CounterStatement();
        JFirewall.ruleAdd(c1Chain, expressionList, List.of(verdictStatement, logStatement, counterStatement), "Drop any request on protocol ip and port 54");
        Assertions.assertFalse(JFirewall.ruleList().isEmpty());
    }

    @Test
    @Order(101)
    public void checkRuleAdd2() {
        logger.info("Execute <checkRuleAdd2>");
        Table table = JFirewall.table("SecondTable", TableType.INET);
        Chain c1Chain = JFirewall.chainAdd(table, "c1", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);

        List<Expression> expressionList = new ArrayList<>();
        Expression ipExpression = new IpExpression(IpExpression.Field.PROTOCOL, Operation.EQ, List.of(Protocols.IP.getValue()));
        Expression tcpExpression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("54"));

        /* CT Expression */
        Expression ctExpression1 = new CtExpression(CtExpression.Field.STATE, Operation.EQ, List.of(CtExpression.State.ESTABLISHED.getValue()));
        Expression ctExpression2 = new CtExpression(CtExpression.Field.STATUS, Operation.EQ, List.of(CtExpression.Status.CONFIRMED.getValue()));
        Expression ctExpression3 = new CtExpression(true, CtExpression.Type.PROTO_SRC, List.of("120", "54"));
        Expression ctExpression4 = new CtExpression(false, CtExpression.Type.DADDR, List.of("10.10.10.12"));
        Expression ctExpression5 = new CtExpression(true, 30L);

        expressionList.add(ipExpression);
        expressionList.add(tcpExpression);
        expressionList.add(ctExpression1);
        expressionList.add(ctExpression2);
        expressionList.add(ctExpression3);
        expressionList.add(ctExpression4);
        expressionList.add(ctExpression5);

        Statement statement = new VerdictStatement(VerdictStatement.Type.CONTINUE);
        JFirewall.ruleAdd(c1Chain, expressionList, List.of(statement), "Check first rule");
        Assertions.assertFalse(JFirewall.ruleList().isEmpty());
    }

    @Test
    @Order(102)
    public void checkRuleAdd3() {
        logger.info("Execute <checkRuleAdd3>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        {
            Chain logAndDropChain = JFirewall.chainAdd(table, "logging");
            Statement logStatement = new LogStatement(LogStatement.LogLevel.ALERT);
            JFirewall.ruleAdd(logAndDropChain, null, List.of(logStatement), "Log traffic");
        }

        {
            Chain c1Chain = JFirewall.chainAdd(table, "c1", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);
            List<Expression> expressionList = new ArrayList<>();
            TcpExpression tcpExpression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("443"));

            expressionList.add(tcpExpression);

            Statement statement = new VerdictStatement(VerdictStatement.Type.JUMP, "logging");
            JFirewall.ruleAdd(c1Chain, expressionList, List.of(statement), "Check first rule");
            Assertions.assertFalse(JFirewall.ruleList().isEmpty());
        }
    }

    @Test
    @Order(103)
    public void checkRuleAdd4() {
        logger.info("Execute <checkRuleAdd4>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain translateChain = JFirewall.chainAdd(table, "translate");
        Expression expression = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("123"));
        Statement natStatement = new NatStatement(NatStatement.Type.SNAT, "10.10.10.12", 25, List.of(NatStatement.Flag.PERSISTENT, NatStatement.Flag.FULLY_RANDOM));
        JFirewall.ruleAdd(translateChain, List.of(expression), List.of(natStatement), "Source NAT");
    }

    @Test
    @Order(104)
    public void checkRuleAdd5() {
        logger.info("Execute <checkRuleAdd5>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain translateChain = JFirewall.chainAdd(table, "translate");
        Expression expression = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("125"));
        Statement natStatement = new NatStatement(NatStatement.Type.REDIRECT, 26, List.of(NatStatement.Flag.PERSISTENT, NatStatement.Flag.FULLY_RANDOM));
        JFirewall.ruleAdd(translateChain, List.of(expression), List.of(natStatement), "Redirect");
    }

    @Test
    @Order(105)
    public void checkRuleAdd6() {
        logger.info("Execute <checkRuleAdd6>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain translateChain = JFirewall.chainAdd(table, "translate");
        Expression expression = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("220"));
        Statement natStatement = new NatStatement(List.of(NatStatement.Flag.PERSISTENT, NatStatement.Flag.FULLY_RANDOM));
        JFirewall.ruleAdd(translateChain, List.of(expression), List.of(natStatement), "Masquerade");
    }

    @Test
    @Order(106)
    public void checkRuleAdd7() {
        logger.info("Execute <checkRuleAdd7>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain counterChain = JFirewall.chainAdd(table, "count");
        Expression expression = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("443"));
        Statement statement = new CounterStatement();
        JFirewall.ruleAdd(counterChain, List.of(expression), List.of(statement), "Counter");
    }

    @Test
    @Order(107)
    public void checkRuleAdd8() {
        logger.info("Execute <checkRuleAdd8>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain rejectChain = JFirewall.chainAdd(table, "rejectRequest");
        Expression expression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("1100"));
        Statement statement = new RejectStatement(RejectStatement.Type.ICMP, RejectStatement.Reason.ADMIN_PROHIBITED);
        JFirewall.ruleAdd(rejectChain, List.of(expression), List.of(statement), "reject packets on sport 1100");
    }

    @Test
    @Order(108)
    public void checkRuleAdd9() {
        logger.info("Execute <checkRuleAdd9>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain limitChain = JFirewall.chainAdd(table, "rejectRequest");
        Expression expression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("1100"));
        Statement statement = new LimitStatement(12L, LimitStatement.TimeUnit.MINUTE, LimitStatement.ByteUnit.MBYTES, true, 12L, LimitStatement.ByteUnit.KBYTES);
        JFirewall.ruleAdd(limitChain, List.of(expression), List.of(statement), "Limit statement 1");
    }

    @Test
    @Order(109)
    public void checkRuleAdd10() {
        logger.info("Execute <checkRuleAdd10>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain translateChain = JFirewall.chainAdd(table, "translate");
        Expression expression = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("220"));
        Statement natStatement = new NatStatement(null);
        JFirewall.ruleAdd(translateChain, List.of(expression), List.of(natStatement), "Masquerade2");
    }

    @Test
    @Order(150)
    public void checkRuleInsert() {
        logger.info("Execute <checkRuleInsert>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain chain = JFirewall.chain(table, "c1");

        Rule currentRule = JFirewall.ruleList().stream().filter(item -> item.getChain().equals(chain)).findFirst().orElse(null);

        List<Expression> expressionList = new ArrayList<>();
        IpExpression ipExpression = new IpExpression(IpExpression.Field.PROTOCOL, Operation.EQ, List.of(Protocols.IP.getValue(), Protocols.MOBILITY_HEADER.getValue(), Protocols.VRRP.getValue(), Protocols.RDP.getValue()));
        TcpExpression tcpExpression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("114", "80", "24"));
        Expression etherExpression = new EtherExpression(EtherExpression.Field.SADDR, Operation.EQ, List.of("00:0f:54:0c:11:04", "00:0f:54:0c:11:12"));
        Expression etherExpression2 = new EtherExpression(List.of(EtherExpression.Type.VLAN, EtherExpression.Type.IP));

        expressionList.add(ipExpression);
        expressionList.add(tcpExpression);
        expressionList.add(etherExpression);
        expressionList.add(etherExpression2);

        Statement statement = new VerdictStatement(VerdictStatement.Type.ACCEPT);
        Assertions.assertNotNull(currentRule);
        JFirewall.ruleInsert(chain, expressionList, List.of(statement), "Check Insert rule", currentRule.getHandle());
        Assertions.assertFalse(JFirewall.ruleList().isEmpty());
    }

    @Test
    @Order(200)
    public void checkRulesFindByChain() {
        logger.info("Execute <checkRulesFindByChain>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        Chain chain = JFirewall.chain(table, "c1");
        List<Rule> rules = JFirewall.ruleList(chain);
        Assertions.assertFalse(rules.isEmpty());
    }

    @Test
    @Order(201)
    public void checkTableRename() {
        logger.info("Execute <checkTableRename>");
        Table table = JFirewall.table("ThirdTable", TableType.IPv4);
        JFirewall.tableRename(table.getHandle(), "NewTableName");
        Assertions.assertTrue(JFirewall.tableList().stream().anyMatch(item -> item.getName().equals("NewTableName")));
    }

    @Test
    @Order(202)
    public void checkRuleSwitch() {
        logger.info("Execute <checkRuleSwitch>");
        Table table = JFirewall.tableAdd("CheckSwitch", TableType.INET);
        Chain chain = JFirewall.chainAdd(table, "SwitchRules");

        List<Expression> expressionList1 = List.of(new IpExpression(IpExpression.Field.SADDR, Operation.EQ, List.of("20.20.20.12")));
        Statement statement1 = new VerdictStatement(VerdictStatement.Type.DROP);
        JFirewall.ruleAdd(chain, expressionList1, List.of(statement1), "R1");

        List<Expression> expressionList2 = List.of(new IpExpression(IpExpression.Field.SADDR, Operation.EQ, List.of("30.30.30.55")));
        Statement statement2 = new VerdictStatement(VerdictStatement.Type.ACCEPT);
        JFirewall.ruleAdd(chain, expressionList2, List.of(statement2), "R2");

        Rule r1 = JFirewall.ruleList(chain).stream().filter(item -> item.getComment().equals("R1")).toList().getFirst();
        Rule r2 = JFirewall.ruleList(chain).stream().filter(item -> item.getComment().equals("R2")).toList().getFirst();

        JFirewall.ruleSwitch(chain, r2.getHandle(), r1.getHandle());
    }

    @Test
    @Order(300)
    public void checkSaveFirewall() {
        File file = new File("/tmp/jfirewall.json");
        JFirewall.backup(file);
        Assertions.assertTrue(file.exists());
    }

    @Test
    @Order(301)
    public void checkSerializeByteCode() {
        try {
            NFTables nfTables = JFirewall.exportToNFTables();
            byte[] bytes = JFirewall.serializeByteCode(nfTables);

            // write byte code to file
            Path path = Path.of("/tmp/jfirewall.bytes");
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(302)
    public void checkSerializeJson() {
        NFTables nfTables = JFirewall.exportToNFTables();
        Assertions.assertDoesNotThrow(() -> JFirewall.serializeJson(nfTables));
    }

    @Test
    @Order(302)
    public void checkLoadSerializedJson() {
        try {
            Path path = Path.of("/tmp/jfirewall.json");
            String json = Files.readString(path);
            Assertions.assertDoesNotThrow(() -> JFirewall.deserializeJson(json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(400)
    public void checkNFTablesObject() {
        NFTables nfTables = JFirewall.nfTables();
        Assertions.assertNotNull(nfTables);
        Assertions.assertFalse(nfTables.getTables().isEmpty());
    }

    @Test
    @Order(500)
    public void checkTableRemove() {
        logger.info("Execute <checkTableRemove>");
        Table table = JFirewall.table("SecondTable", TableType.INET);
        JFirewall.tableRemove(table);
    }

    @Test
    @Order(501)
    public void checkTableExists() {
        logger.info("Execute <checkTableExists>");
        Table first = JFirewall.tableList().getFirst();
        Assertions.assertNotNull(first);
        JFirewall.tableCheckExists(first.getHandle());
    }

    @Test
    @Order(502)
    public void checkChainRemove() {
        logger.info("Execute <checkChainRemove>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Chain chain = JFirewall.chain(table, "c1");
        JFirewall.chainRemove(chain);
    }

    @Test
    @Order(503)
    public void checkSetRemoveItem() {
        logger.info("Execute <checkSetRemoveItem>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");
        JFirewall.setRemoveElement(localNetworkSet, List.of("192.168.1.10", "127.0.0.1"));

        Set localNetworkSetAfterUpdate = JFirewall.set(table, "localNetworkSet");
        Assertions.assertEquals(1, localNetworkSetAfterUpdate.getElements().size());
    }


    @Test
    @Order(504)
    public void checkSetRemove() {
        logger.info("Execute <checkSetRemove>");
        Table table = JFirewall.table("FirstTable", TableType.IPv4);
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");

        JFirewall.setRemove(localNetworkSet);
    }

    @Test
    @Order(505)
    public void checkRuleRemove() {
        logger.info("Execute <checkRuleRemove>");
        Table table = JFirewall.table("NewTableName", TableType.IPv4);
        Chain chain = JFirewall.chain(table, "c1");

        Rule rule = JFirewall.ruleList(chain).getFirst();
        JFirewall.ruleRemove(chain, rule.getHandle());
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.ruleCheckExists(rule.getChain(), rule.getHandle()));
    }
}
