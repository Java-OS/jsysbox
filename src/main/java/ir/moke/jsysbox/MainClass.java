/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.moke.jsysbox;

import ir.moke.jsysbox.firewall.JFirewall;
import ir.moke.jsysbox.firewall.expression.*;
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.Statement;
import ir.moke.jsysbox.firewall.statement.VerdictStatement;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainClass {
    private static final File file = new File("/home/mah454/test/config.nft");

    public static void main(String[] args) throws IOException {
        JFirewall.flush(TableType.IPv4);

        Table filterTable = JFirewall.tableAdd("filter", TableType.IPv4);
        Chain chain = JFirewall.chainAdd(filterTable, "input", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);

//        Expression expr1 = new Expression(MatchType.IP, Field.SADDR, Operation.EQ, List.of("10.20.20.12", "20.20.20.1"));
        Expression expr1 = new TcpExpression(TcpExpression.Field.DPORT, Operation.EQ, List.of("22"));
        Statement stt1 = new VerdictStatement(VerdictStatement.Type.DROP, chain);
        JFirewall.ruleAdd(chain, List.of(expr1), stt1, "First Rule");

        Expression expr2 = new IpExpression(IpExpression.Field.SADDR, Operation.EQ, List.of("20.21.12.12"));
        Expression expr3 = new IpExpression(IpExpression.Field.PROTOCOL, Operation.EQ, List.of(Protocols.IP.name()));
        Expression expr4 = new TcpExpression(TcpExpression.Field.ACKSEQ, Operation.EQ, List.of("22"));
        Expression expr5 = new MetaExpression(MetaExpression.Field.MARK, Operation.EQ, List.of("0x4"));
        Expression expr6 = new MetaExpression(MetaExpression.Field.SKGID, Operation.EQ, List.of("12"));
        Expression expr7 = new CtExpression(CtExpression.Field.STATUS, Operation.EQ, List.of(CtExpression.Status.SEEN_REPLY.getValue()));
        Expression expr8 = new VlanExpression(VlanExpression.Field.ID, Operation.EQ, List.of("8"));
        Expression expr9 = new VlanExpression(VlanExpression.Field.PCP, Operation.EQ, List.of("2"));
        Expression expr10 = new CtExpression(true, CtExpression.Type.SADDR, List.of("10.10.11.11"));
        Expression expr11 = new CtExpression(true, 12);
        Statement stt2 = new VerdictStatement(VerdictStatement.Type.ACCEPT, chain);
        JFirewall.ruleAdd(chain, List.of(expr2, expr3, expr4, expr5, expr6, expr7, expr8, expr9, expr10, expr11), stt2, "Second Rule");

        List<Rule> rules = JFirewall.ruleList();
        for (Rule rule : rules) {
            System.out.println(rule);
        }

        JFirewall.save(file);
    }
}