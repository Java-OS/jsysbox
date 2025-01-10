package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.moke.jsysbox.firewall.config.deserializer.NFTablesDeserializer;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = NFTablesDeserializer.class)
public class NFTables {
    private MetaInfo metaInfo;
    private List<Table> tables = new ArrayList<>();
    private List<Chain> chains = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private List<Set> sets = new ArrayList<>();

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
