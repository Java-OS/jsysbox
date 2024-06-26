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
import ir.moke.jsysbox.firewall.model.Chain;
import ir.moke.jsysbox.firewall.model.Set;

import java.io.IOException;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws IOException {
        List<Chain> chains = JFirewall.chainList();
        for (Chain chain : chains) {
            System.out.println(chain);
        }

        List<Set> sets = JFirewall.setList();
        sets.forEach(System.out::println);

        if (!sets.isEmpty()) {
            JFirewall.setAddElement(sets.get(0), List.of("TCP", "UDP", "ICMP"));
            JFirewall.setRemoveElement(sets.get(0), List.of("TCP"));
            JFirewall.setRemove(sets.get(0));
        }


        //        JFirewall.restore("/home/mah454/firewall/config.nft");
//        List<Chain> chains = JFirewall.listChain();
//        System.out.println(chains);

//        if (!chains.isEmpty()) {
//            Chain chain = chains.get(0);
//            JFirewall.removeChain(chain.getTable(), chain.getFamily(), chain.getHandle());
//        }
    }
}