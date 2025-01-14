package ir.moke.jsysbox.firewall.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.moke.jsysbox.JSysboxException;
import ir.moke.jsysbox.firewall.config.deserializer.NFTablesDeserializer;
import ir.moke.jsysbox.firewall.config.serializer.NFTablesSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = NFTablesDeserializer.class)
@JsonSerialize(using = NFTablesSerializer.class)
public class NFTables implements Serializable {
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

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JSysboxException(e);
        }
    }
}
