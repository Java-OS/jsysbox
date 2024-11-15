package ir.moke;

import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.expression.Expression;
import ir.moke.jsysbox.firewall.expression.IpExpression;
import ir.moke.jsysbox.firewall.expression.TcpExpression;
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.Statement;
import ir.moke.jsysbox.firewall.statement.VerdictStatement;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FirewallTest {
    private static final Logger logger = LoggerFactory.getLogger(FirewallTest.class);

    @Test
    @Order(0)
    public void checkTableAdd() {
        logger.info("Execute <checkTableAdd>");
        Table ipv4Table = JFirewall.tableAdd("FirstTable", TableType.IPv4);
        Assertions.assertEquals("FirstTable", ipv4Table.getName());
        Assertions.assertEquals(TableType.IPv4, ipv4Table.getType());

        Table inetTable = JFirewall.tableAdd("SecondTable", TableType.INET);
        Assertions.assertEquals("SecondTable", inetTable.getName());
        Assertions.assertEquals(TableType.INET, inetTable.getType());
    }

    @Test
    @Order(1)
    public void checkTableList() {
        logger.info("Execute <checkTableList>");
        List<Table> tables = JFirewall.tableList();
        Assertions.assertEquals(2, tables.size());
    }

    @Test
    @Order(2)
    public void checkTableByName() {
        logger.info("Execute <checkTableByName>");
        Table table = JFirewall.table("FirstTable");
        Assertions.assertNotNull(table);
    }

    @Test
    @Order(3)
    public void checkTableRemove() {
        logger.info("Execute <checkTableRemove>");
        Table table = JFirewall.table("SecondTable");
        JFirewall.tableRemove(table);
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.table("SecondTable"));
    }

    @Test
    @Order(4)
    public void checkTableExists() {
        logger.info("Execute <checkTableExists>");
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.tableCheckExists("SecondTable"));
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
    @Order(5)
    public void checkDuplicateTableName() {
        logger.info("Execute <checkDuplicateTableName>");
        Table table = JFirewall.tableAdd("FirstTable", TableType.IPv4);
        Assertions.assertNotNull(table);
    }

    @Test
    @Order(6)
    public void checkChainAdd() {
        logger.info("Execute <checkChainAdd>");
        Table table = JFirewall.table("FirstTable");
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
    @Order(7)
    public void checkChainList() {
        logger.info("Execute <checkChainList>");
        List<Chain> chains = JFirewall.chainList();
        Assertions.assertEquals(2, chains.size());
    }

    @Test
    @Order(8)
    public void checkChainRemove() {
        logger.info("Execute <checkChainRemove>");
        Table table = JFirewall.table("FirstTable");
        Chain chain = JFirewall.chain(table, "c1");
        JFirewall.chainRemove(chain);
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.chainCheckExists(chain.getHandle()));
    }

    @Test
    @Order(9)
    public void checkSetAdd() {
        logger.info("Execute <checkSetAdd>");
        Table table = JFirewall.table("FirstTable");

        Set set = JFirewall.setAdd(table.getType(), table.getName(), "localNetworkSet", SetType.IPV4_ADDR, List.of(FlagType.INTERVAL), 20, 2, 30, "My Local Network", SetPolicy.MEMORY, false);
        Assertions.assertNotNull(set);
        Assertions.assertEquals(set.getTable(), table);
        Assertions.assertEquals(set.getName(), "localNetworkSet");
    }

    @Test
    @Order(10)
    public void checkSetExists() {
        logger.info("Execute <checkSetExists>");
        Table table = JFirewall.table("FirstTable");
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");
        Assertions.assertNotNull(localNetworkSet);
    }

    @Test
    @Order(10)
    public void checkSetAddItem() {
        logger.info("Execute <checkSetAddItem>");
        Table table = JFirewall.table("FirstTable");
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
    @Order(11)
    public void checkSetRemoveItem() {
        logger.info("Execute <checkSetRemoveItem>");
        Table table = JFirewall.table("FirstTable");
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");
        JFirewall.setRemoveElement(localNetworkSet, List.of("192.168.1.10", "127.0.0.1"));

        Set localNetworkSetAfterUpdate = JFirewall.set(table, "localNetworkSet");
        Assertions.assertEquals(1, localNetworkSetAfterUpdate.getElements().size());
    }

    @Test
    @Order(12)
    public void checkSetRemove() {
        logger.info("Execute <checkSetRemove>");
        Table table = JFirewall.table("FirstTable");
        Set localNetworkSet = JFirewall.set(table, "localNetworkSet");

        JFirewall.setRemove(localNetworkSet);
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.setCheckExists(localNetworkSet.getHandle()));
    }

    @Test
    @Order(13)
    public void checkRuleAdd() {
        logger.info("Execute <checkRuleAdd>");
        Table table = JFirewall.table("FirstTable");
        Chain c1Chain = JFirewall.chainAdd(table, "c1", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);

        List<Expression> expressionList = new ArrayList<>();
        IpExpression ipExpression = new IpExpression(IpExpression.Field.PROTOCOL, Operation.EQ, List.of(Protocols.IP.getValue()));
        TcpExpression tcpExpression = new TcpExpression(TcpExpression.Field.SPORT, Operation.EQ, List.of("54"));

        expressionList.add(ipExpression);
        expressionList.add(tcpExpression);

        Statement statement = new VerdictStatement(VerdictStatement.Type.DROP);
        JFirewall.ruleAdd(c1Chain, expressionList, statement, "Drop any request on protocol ip and port 54");
        Assertions.assertFalse(JFirewall.ruleList().isEmpty());
    }

    @Test
    @Order(14)
    public void checkRuleExists() {
        logger.info("Execute <checkRuleExists>");
        Rule rule = JFirewall.ruleList().getFirst();
        Assertions.assertDoesNotThrow(() -> JFirewall.ruleCheckExists(rule.getHandle()));
    }

    @Test
    @Order(15)
    public void checkRulesFindByChain() {
        logger.info("Execute <checkRulesFindByChain>");
        Table table = JFirewall.table("FirstTable");
        Chain chain = JFirewall.chain(table, "c1");
        List<Rule> ruleList = JFirewall.findRulesByChain(chain);
        Assertions.assertFalse(ruleList.isEmpty());
    }

    @Test
    @Order(16)
    public void checkRuleRemove() {
        logger.info("Execute <checkRuleRemove>");
        Table table = JFirewall.table("FirstTable");
        Chain chain = JFirewall.chain(table, "c1");

        Rule rule = JFirewall.ruleList().getFirst();
        JFirewall.ruleRemove(chain, rule.getHandle());
        Assertions.assertThrows(JSysboxException.class, () -> JFirewall.ruleCheckExists(rule.getHandle()));
    }
}
