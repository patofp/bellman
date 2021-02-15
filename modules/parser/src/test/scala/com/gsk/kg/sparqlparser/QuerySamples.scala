package com.gsk.kg.sparqlparser

object QuerySamples {

  // Get a small sample of documents from the KG
  val q1 = """
    SELECT ?docid ?doi WHERE {
        ?docid a <http://gsk-kg.rdip.gsk.com/dm/1.0/Document> .
        ?docid <http://prismstandard.org/namespaces/basic/2.0/doi> ?doi .
    } LIMIT 20
    """

  // Find the label for an ontology term (NICR2579 cell)
  val q2 = """
    SELECT ?label WHERE {
        <http://purl.obolibrary.org/obo/CLO_0052588>
        <http://www.w3.org/2000/01/rdf-schema#label>
        ?label.
    }
    """

  // Find the distinct labels for an ontology term (NICR2579 cell)
  // CLO terms will generally only be in CLO, this should be the same as above
  val q3 = """
    SELECT DISTINCT ?label WHERE {
        <http://purl.obolibrary.org/obo/CLO_0052588>
        <http://www.w3.org/2000/01/rdf-schema#label>
        ?label.
    }
    """

  // Get all relations of NICR2579 cell
  // None of these should be blank nodes
  val q4 = "SELECT ?p ?o WHERE { <http://purl.obolibrary.org/obo/CLO_0052588> ?p ?o .}"

  // Get parent classes of a class of cell lines
  // This includes a distinct IRI and a BNode
  val q5 = """
    SELECT ?parent WHERE {
        <http://purl.obolibrary.org/obo/CLO_0037232>
        <http://www.w3.org/2000/01/rdf-schema#subClassOf>
        ?parent.
    }
    """

  // Get parent classes of a class of cell lines
  // Filter out BNode result
  val q6 = """
    SELECT ?parent WHERE {
        {
            <http://purl.obolibrary.org/obo/CLO_0037232>
            <http://www.w3.org/2000/01/rdf-schema#subClassOf>
            ?parent .
        }
        FILTER (!isBlank(?parent))
    }
    """

  // Get species source of a cell line
  // Tests multiple hops
  val q7 = """
    SELECT ?species WHERE {
        <http://purl.obolibrary.org/obo/CLO_0037232> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?derived_node .
        ?derived_node a <http://www.w3.org/2002/07/owl#Restriction>;
            <http://www.w3.org/2002/07/owl#onProperty> <http://purl.obolibrary.org/obo/RO_0001000>;
            <http://www.w3.org/2002/07/owl#someValuesFrom> ?species_node .
        ?species_node a <http://www.w3.org/2002/07/owl#Restriction>;
            <http://www.w3.org/2002/07/owl#onProperty> <http://purl.obolibrary.org/obo/BFO_0000050>;
            <http://www.w3.org/2002/07/owl#someValuesFrom> ?species .
    }
    """

  // Get species source of a cell line
  // Prefixed to reduce query length
  // Tests multiple hops and prefixes
  val q8 = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    PREFIX obo-term: <http://purl.obolibrary.org/obo/>
    SELECT ?species WHERE {
        obo-term:CLO_0037232 rdfs:subClassOf ?derived_node .
        ?derived_node a owl:Restriction;
            owl:onProperty obo-term:RO_0001000;
            owl:someValuesFrom ?species_node .
        ?species_node a owl:Restriction;
            owl:onProperty obo-term:BFO_0000050;
            owl:someValuesFrom ?species .
    }
    """

  // Find the label for an ontology term (respiration organ)
  val q9 = """
    SELECT ?label WHERE {
        <http://purl.obolibrary.org/obo/UBERON_0000171>
        <http://www.w3.org/2000/01/rdf-schema#label>
        ?label .
    }
    """

  // Get parent classes of respiration organ
  val q10 = """
    SELECT ?parent WHERE {
        <http://purl.obolibrary.org/obo/UBERON_0000171>
        <http://www.w3.org/2000/01/rdf-schema#subClassOf>
        ?parent .
    }
    """

  // Get the label for the parent of respiration organ (organ)
  // Tests multiple hops and DISTINCT
  val q11 = """
    SELECT DISTINCT ?parent_name WHERE {
        <http://purl.obolibrary.org/obo/UBERON_0000171>
        <http://www.w3.org/2000/01/rdf-schema#subClassOf>
        ?parent .
        ?parent <http://www.w3.org/2000/01/rdf-schema#label> ?parent_name .
    }
    """

  // Get children of lung, returning lung in the output
  // Tests BIND
  val q12 =  """
    SELECT ?s ?p ?o WHERE {
        ?s ?p <http://purl.obolibrary.org/obo/UBERON_0002048> .
        FILTER (!isBlank(?s)) .
        BIND (<http://purl.obolibrary.org/obo/UBERON_0002048> AS ?o)
    }
    """

  // Test BIND in another position in the query
  // Result should be the same as previous
  val q13 = """
    SELECT ?s ?p ?o WHERE {
        BIND (<http://purl.obolibrary.org/obo/UBERON_0002048> AS ?o)
        ?s ?p <http://purl.obolibrary.org/obo/UBERON_0002048> .
        FILTER (!isBlank(?s)) .
    }
    """

  // Get triples referring to lung either as subject or object
  // Test UNION
  val q14 = """
    SELECT ?s ?p ?o WHERE {
        {
            ?s ?p <http://purl.obolibrary.org/obo/UBERON_0002048> .
            FILTER (!isBlank(?s)) .
            BIND (<http://purl.obolibrary.org/obo/UBERON_0002048> AS ?o)
        } UNION {
            <http://purl.obolibrary.org/obo/UBERON_0002048> ?p ?o .
            FILTER (!isBlank(?o)) .
            BIND (<http://purl.obolibrary.org/obo/UBERON_0002048> AS ?s)
        }
    }
    """

  // Describe lung
  val q15 = "DESCRIBE <http://purl.obolibrary.org/obo/UBERON_0002048>"

  // Describe the non-blank children of lung
  val q16 = """
    DESCRIBE ?s WHERE {
        ?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/UBERON_0002048> .
        FILTER (!isBlank(?s)) .
    }
    """

  // Concept mapping query - gets info on the UMLS concept links
  // Tests str conversion and logical operators
  val q17 =  """
    SELECT ?s ?o WHERE {
        ?s a <http://gsk-kg.rdip.gsk.com/dm/1.0/UMLSConcept> .
        ?s <http://gsk-kg.rdip.gsk.com/dm/1.0/conceptLink> ?o .
        FILTER (regex(str(?o),"https://meshb.nlm.nih.gov/record/ui")
            || regex(str(?o),"https://www.ncbi.nlm.nih.gov/taxonomy/")
            || regex(str(?o),"https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=")
        ) .
    }
    """

  // Larger union with filter
  // Tests FILTER positioning with graph sub-patterns
  val q18 = """
    SELECT ?s ?o WHERE {
        ?s a <http://gsk-kg.rdip.gsk.com/dm/1.0/UMLSConcept> .
        {
            ?s <http://gsk-kg.rdip.gsk.com/dm/1.0/conceptLink> ?o .
        } UNION {
            ?s <http://gsk-kg.rdip.gsk.com/dm/1.0/stringLiteral> ?o .
        }
        FILTER (regex(str(?o),"https://meshb.nlm.nih.gov/record/ui")
            || regex(str(?o),"http://amigo.geneontology.org/amigo/term/")
        ).
    }
    """

  // Collect labels for terms connected to lungs,
  // or label for the concrete term if the node connected is a BNode
  // This is mainly a test for FILTER in different positions
  val q19 = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT DISTINCT ?conc ?ps ?s ?p ?o ?label WHERE {
        ?s ?p <http://purl.obolibrary.org/obo/UBERON_0002048> .
        {
            ?s rdfs:label ?label
            FILTER (!isBlank(?s))
        } UNION {
            ?conc ?ps ?s .
            ?conc rdfs:label ?label .
            FILTER isBlank(?s)
        }
        FILTER regex(?label, "lung")
        BIND (<http://purl.obolibrary.org/obo/UBERON_0002048> AS ?o)
    }
    """

  // Construct a literature mapping result
  // Tests CONSTRUCT and string replacement
  val q20 = """
    PREFIX lita: <http://lit-search-api/attribute/>
    PREFIX dm: <http://gsk-kg.rdip.gsk.com/dm/1.0/>
    CONSTRUCT {?s lita:has_alias ?ostr} WHERE {
        ?s a dm:UMLSConcept .
        ?s dm:conceptLink ?o .
        # Removes the 'MESH:' from the data
        BIND(REPLACE(STR(?o),"^[^=]+=","MESH:") as ?ostr) .
    }
    """

  // Simple "What does the KG say about X" query
  // Can be run over literature-extracted triples.
  val q21 = """
    PREFIX  schema: <http://schema.org/>
    PREFIX  dm:   <http://gsk-kg.rdip.gsk.com/dm/1.0/>
    PREFIX  prism: <http://prismstandard.org/namespaces/basic/2.0/>

    SELECT
        ?doc ?doi ?src ?year ?month ?title ?te ?text ?objText ?objStartIdx ?objEndIdx ?subjText ?subjStartIdx ?subjEndIdx ?subjconcept
    WHERE
    {
        ?subjle dm:mappedTo ?subjconcept .
        ?subjde dm:entityLink ?subjle .
        ?pred dm:hasSubject ?subjde .
        ?pred dm:hasObject ?objde .
        ?objde dm:entityLink ?objle .
        # For example purposes, chose CUI for Aspirin
        ?objle dm:mappedTo <http://gsk-kg.rdip.gsk.com/umls/CUI=C0002370> .
        ?subjde dm:indexStart ?subjStartIdx .
        ?subjde dm:indexEnd ?subjEndIdx .
        ?subjde dm:text ?subjText .
        ?objde dm:indexStart ?objStartIdx .
        ?objde dm:indexStart ?objEndIdx .
        ?objde dm:text ?objText .
        ?te dm:contains ?pred .
        ?te dm:text ?text .
        ?ds dm:contains ?te .
        ?doc dm:contains ?ds .
        ?doc schema:title ?title .
        ?doc dm:docSource ?src .
        ?doc prism:doi ?doi .
        ?doc dm:pubDateYear ?year .
        ?doc dm:pubDateMonth ?month .
    }
    """
}
