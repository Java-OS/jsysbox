package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.firewall.config.NFTablesDeserializer;
import ir.moke.jsysbox.firewall.config.NFTablesSerializer;

import java.util.List;

@JsonDeserialize(using = NFTablesDeserializer.class)
@JsonSerialize(using = NFTablesSerializer.class)
public class NFTables {
    private MetaInfo metaInfo;
    private List<Table> tables;
    private List<Chain> chains;
    private List<Rule> rules;
    private List<Set> sets;

    public NFTables() {
    }

    public NFTables(MetaInfo metaInfo, List<Table> tables, List<Chain> chains, List<Rule> rules, List<Set> sets) {
        this.metaInfo = metaInfo;
        this.tables = tables;
        this.chains = chains;
        this.rules = rules;
        this.sets = sets;
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<Chain> getChains() {
        return chains;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public List<Set> getSets() {
        return sets;
    }
}
