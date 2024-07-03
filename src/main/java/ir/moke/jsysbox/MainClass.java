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
import ir.moke.jsysbox.firewall.model.*;
import ir.moke.jsysbox.firewall.statement.NatStatement;
import ir.moke.jsysbox.firewall.statement.Statement;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws IOException {
        JFirewall.flush(null);
        Table t1 = JFirewall.tableAdd("t1", TableType.IPv4);
        Chain c1 = JFirewall.chainAdd(t1, "c1", ChainType.NAT, ChainHook.PREROUTING, ChainPolicy.ACCEPT, 1);
        Chain c2 = JFirewall.chainAdd(t1, "c2", ChainType.FILTER, ChainHook.INPUT, ChainPolicy.ACCEPT, 1);
        Chain c3 = JFirewall.chainAdd(t1, "c3", ChainType.NAT, ChainHook.POSTROUTING, ChainPolicy.ACCEPT, 1);
        JFirewall.save(new File("/home/mah454/firewall/config.nft"));

        Expression expression1 = new Expression(MatchType.IP, Field.SADDR, Operation.EQ, List.of("4.4.4.4"));
        Expression expression2 = new Expression(MatchType.TCP, Field.DPORT, Operation.EQ, List.of("5151"));
//        Statement statement = new VerdictStatement(VerdictStatement.Type.ACCEPT);
//        Statement statement = LogStatement.ALERT;
//        Statement statement = RejectStatement.ICMP_TYPE_ADMIN_PROHIBITED;
//        Statement statement = new CounterStatement();
//        Statement statement = new LimitStatement(12, LimitStatement.TimeUnit.DAY, true);
//        Statement statement = new LimitStatement(12, LimitStatement.TimeUnit.DAY, LimitStatement.ByteUnit.KBYTES, true);
        Statement statement = new NatStatement(NatStatement.Type.SNAT, "10.10.10.2", null);
        JFirewall.ruleAdd(c1, List.of(expression1, expression2), statement, "this is first rule");
    }
}