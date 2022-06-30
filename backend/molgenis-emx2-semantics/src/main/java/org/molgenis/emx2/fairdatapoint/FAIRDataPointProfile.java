package org.molgenis.emx2.fairdatapoint;

public class FAIRDataPointProfile {

  /*
  As defined by https://specs.fairdatapoint.org/
   */
  public static final String FDP_SHACL =
      """
			@prefix      : <http://fairdatapoint.org/> .
			@prefix  dcat: <http://www.w3.org/ns/dcat#> .
			@prefix   dct: <http://purl.org/dc/terms/> .
			@prefix fdp-o: <http://purl.org/fdp/fdp-o#> .
			@prefix  foaf: <http://xmlns.com/foaf/0.1/> .
			@prefix   ldp: <http://www.w3.org/ns/ldp#> .
			@prefix    sh: <http://www.w3.org/ns/shacl#> .
			@prefix   xsd: <http://www.w3.org/2001/XMLSchema#> .

			:FAIRDataPointShape a sh:NodeShape ;
			  sh:targetClass fdp-o:MetadataService ;
			  sh:property [
			    sh:path dct:title ;
			    sh:nodeKind sh:Literal ;
			    sh:minCount 1 ;
			  ], [
			    sh:path dct:hasVersion ;
			    sh:nodeKind sh:Literal ;
			    sh:maxCount ;
			  ], [
			    sh:path dct:description ;
			    sh:nodeKind sh:Literal ;
			  ], [
			    sh:path fdp-o:metadataCatalog ;
			    sh:node :CatalogShape ;
			    sh:minCount 1;
			  ], [
			    sh:path dct:publisher ;
			    sh:node :AgentShape ;
			    sh:minCount 1;
			  ], [
			    sh:path dct:language ;
			    sh:nodeKind sh:IRI ;
			  ], [
			    sh:path dct:license ;
			    sh:nodeKind sh:IRI ;
			    sh:minCount 1;
			    sh:maxCount 1 ;
			  ], [
			    sh:path dct:conformsTo ;
			    sh:nodeKind sh:IRI ;
			    sh:maxCount 1;
			    sh:minCount 1;
			  ], [
			    sh:path dct:rights ;
			    sh:nodeKind sh:IRI ;
			  ], [
			    sh:path dct:accessRights ;
			    sh:nodeKind sh:IRI ;
			  ], [
			    sh:path dcat:contactPoint ;
			    sh:node sh:ContactPointShape ;
			  ], [
			    sh:path dcat:keyword ;
			    sh:nodeKind sh:Literal ;
			  ], [
			    sh:path dcat:theme ;
			    sh:nodeKind sh:IRI ;
			  ], [
			    sh:path dcat:endPointURL ;
			    sh:nodeKind sh:IRI ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ], [
			    sh:path fdp-o:startDate ;
			    sh:datatype xsd:date ;
			    sh:maxCount 1 ;
			  ], [
			    sh:path fdp-o:endDate ;
			    sh:datatype xsd:date ;
			    sh:maxCount 1 ;
			  ], [
			    sh:path fdp-o:metadataIdentifier ;
			    sh:nodeKind sh:IRI ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ], [
			    sh:path fdp-o:metadataIssued ;
			    sh:datatype xsd:dateTime ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ], [
			    sh:path fdp-o:metadataIdentifier ;
			    sh:datatype xsd:dateTime ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ], [
			    sh:path dct:conformsToFdpSpec ;
			    sh:nodeKind sh:IRI ;
			    sh:maxCount 1;
			    sh:minCount 1;
			  ].

			:AgentShape a sh:NodeShape ;
			  sh:targetClass foaf:Agent ;
			  sh:property [
			    sh:path foaf:name ;
			    sh:nodeKind sh:Literal ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ].

			:ContactPointShape a sh:NodeShape ;
			  sh:targetClass vcard:Kind ;
			  sh:property [
			    sh:path vcard:hasEmail ;
			    sh:nodeKind sh:Literal ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ].

			:FAIRDataPointContainerShape a sh:NodeShape ;
			  sh:targetClass ldp:DirectContainer ;
			  sh:property [
			    sh:name "title" ;
			    sh:description "Name identifying the member resources" ;
			    sh:path dct:title ;
			    sh:nodeKind sh:Literal ;
			    sh:minCount 1 ;
			    sh:uniqueLang true ;
			  ], [
			    sh:name "membership resource" ;
			    sh:description "" ;
			    sh:path ldp:membershipResource ;
			    sh:nodeKind fdp-o:FairDataPoint ;
			    sh:maxCount 1 ;
			  ], [
			    sh:name "has member relation" ;
			    sh:description "The predicate used in the metadata to relate the FAIR Data Point with its member catalogs." ;
			    sh:path ldp:hasMemberRelation ;
			    sh:nodeKind fdp-o:metadataCatalog ;
			    sh:maxCount 1 ;
			    sh:minCount 1 ;
			  ], [
			    sh:name "contains" ;
			    sh:description "A set of triples, maintained by the LDP container, that lists documents created by the LDP container." ;
			    sh:path ldp:contains ;
			    sh:node :CatalogShape ;
			  ].""";

  /*
  As defined by https://specs.fairdatapoint.org/
   */
  public static final String CATALOG_SHACL =
      """
		  @prefix     : <http://fairdatapoint.org/> .
		  @prefix dcat: <http://www.w3.org/ns/dcat#> .
		  @prefix  dct: <http://purl.org/dc/terms/> .
		  @prefix foaf: <http://xmlns.com/foaf/0.1/> .
		  @prefix   sh: <http://www.w3.org/ns/shacl#> .
		  @prefix  xsd: <http://www.w3.org/2001/XMLSchema#> .

		  :CatalogShape a sh:NodeShape ;
		    sh:targetClass dcat:Catalog ;
		    sh:property [
		      sh:path dct:title ;
		      sh:nodeKind sh:Literal ;
		      sh:minCount 1 ;
		    ], [
		      sh:path dct:hasVersion ;
		      sh:nodeKind sh:Literal ;
		      sh:maxCount ;
		    ], [
		      sh:path dct:description ;
		      sh:nodeKind sh:Literal ;
		    ], [
		      sh:path dct:publisher ;
		      sh:node :AgentShape ;
		      sh:minCount 1;
		    ], [
		      sh:path dct:language ;
		      sh:nodeKind sh:IRI ;
		    ], [
		      sh:path dct:license ;
		      sh:nodeKind sh:IRI ;
		      sh:minCount 1;
		      sh:maxCount 1 ;
		    ], [
		      sh:path dct:conformsTo ;
		      sh:nodeKind sh:IRI ;
		      sh:maxCount 1;
		      sh:minCount 1;
		    ], [
		      sh:path dct:rights ;
		      sh:nodeKind sh:IRI ;
		    ], [
		      sh:path dct:accessRights ;
		      sh:nodeKind sh:IRI ;
		    ], [
		      sh:path dct:hasPart ;
		      sh:node sh:ContactPointShape ;
		    ], [
		      sh:path dcat:keyword ;
		      sh:nodeKind sh:Literal ;
		    ], [
		      sh:path dcat:theme ;
		      sh:nodeKind sh:IRI ;
		    ], [
		      sh:path dcat:endPointURL ;
		      sh:nodeKind sh:IRI ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ], [
		      sh:path fdp-o:startDate ;
		      sh:datatype xsd:date ;
		      sh:maxCount 1 ;
		    ], [
		      sh:path fdp-o:endDate ;
		      sh:datatype xsd:date ;
		      sh:maxCount 1 ;
		    ], [
		      sh:path fdp-o:metadataIdentifier ;
		      sh:nodeKind sh:IRI ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ], [
		      sh:path fdp-o:metadataIssued ;
		      sh:datatype xsd:dateTime ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ], [
		      sh:path fdp-o:metadataIdentifier ;
		      sh:datatype xsd:dateTime ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ].

		  :AgentShape a sh:NodeShape ;
		    sh:targetClass foaf:Agent ;
		    sh:property [
		      sh:path foaf:name ;
		      sh:nodeKind sh:Literal ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ].

		  :ContactPointShape a sh:NodeShape ;
		    sh:targetClass vcard:Kind ;
		    sh:property [
		      sh:path vcard:hasEmail ;
		      sh:nodeKind sh:Literal ;
		      sh:maxCount 1 ;
		      sh:minCount 1 ;
		    ].""";
}
