package it.polito.tdp.borders.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.borders.db.BordersDAO;

public class Model {
	
	private BordersDAO dao;
	private List<Country> countries;
	private CountryIdMap countryIdMap;
	private SimpleGraph<Country, DefaultEdge> grafo;

	public Model() {
		dao = new BordersDAO();	
	}
	
	public void creaGrafo (int anno) {
		
		countryIdMap = new CountryIdMap();
		countries = dao.loadAllCountries(countryIdMap);
		
		List<Border> confini = dao.getCountryPairs(countryIdMap, anno);
		
		if(confini.isEmpty()) {
			throw new RuntimeException("Non sono presenti confini per l'ano selezionato");
		}
		
		grafo = new SimpleGraph<>(DefaultEdge.class);
		
		for(Border b : confini) {
			grafo.addVertex(b.getC1());
			grafo.addVertex(b.getC2());
			grafo.addEdge(b.getC1(), b.getC2());
		}
		
		System.out.format("Inseriti: %d vertici, %d archi\n", grafo.vertexSet().size(), grafo.edgeSet().size());
		
		// Sort the countries
		countries = new ArrayList<>(grafo.vertexSet());
		Collections.sort(countries);
	}
	
	public List<Country> getCountries() {
		if(countries == null) {
			return new ArrayList<Country>();
		}
		
		return countries;
	}
	
	public Map<Country, Integer> getCountryCounts() {
		if(grafo == null) {
			throw new RuntimeException("Grafo non esistente");
		}
		
		Map<Country, Integer> stats = new HashMap<Country, Integer>();
		for(Country country : grafo.vertexSet()) {
			stats.put(country, grafo.degreeOf(country));
		}
		
		return stats;
	}
	
	public int getNumberOfConnectedComponents() {
		if(grafo == null) {
			throw new RuntimeException("Grafo non esistente");
		}
		ConnectivityInspector<Country, DefaultEdge> ci = new ConnectivityInspector<Country, DefaultEdge>(grafo);
		return ci.connectedSets().size();
	}

	public List<Country> getReachableCountries(Country selectedCountry){
		if(!grafo.vertexSet().contains(selectedCountry)) {
			throw new RuntimeException("Selected Country not in graph");
		}
		
		List<Country> reachableCountries = this.displayAllNeighboursIterative(selectedCountry);
		System.out.println("Reachable countries: " + reachableCountries.size());
		reachableCountries = this.displayAllNeighboursJGraphT(selectedCountry);
		System.out.println("Reachable countries: " + reachableCountries.size());
		reachableCountries = this.displayAllNeighboursRecursive(selectedCountry);
		System.out.println("Reachable countries: " + reachableCountries.size());
		
		return reachableCountries;
	}

	private List<Country> displayAllNeighboursIterative(Country selectedCountry) {
		
		// Creo due liste: quella dei nodi visitati ..
		List<Country> visited = new LinkedList<Country>();
		
		// .. e quella dei nodi da visitare
		List<Country> toBeVisited = new LinkedList<Country>();
		
		// Aggiungo alla lista dei vertici visitati il nodo di partenza.
		visited.add(selectedCountry);
		
		// Aggiungo ai vertici da visitare tutti i vertici collegati a quello
		toBeVisited.addAll(Graphs.neighborListOf(grafo, selectedCountry));
		
		while(!toBeVisited.isEmpty()) {
			
			// Rimuovi il vertice in testa alla coda
			Country temp = toBeVisited.remove(0);
			
			// Aggiungi il nodo alla lista di quelli visitati
			visited.add(temp);
			
			// Ottieni tutti i vicini di un nodo
			List<Country> listaDeiVicini = Graphs.neighborListOf(grafo, temp);
			
			// Rimuovi da questa lista tutti quelli che sai gia che devi visitare
			listaDeiVicini.removeAll(toBeVisited);
			
			// Aggiungi i rimamenti alla coda di quelli che devi visitare
			toBeVisited.addAll(listaDeiVicini);
		}
		
		// Ritorna la lista di tutti i nodi raggiungibili
		return visited;
	}
	
	/*
	 *  VERSIONE LIBRERIA JGRAPHT
	 */
	private List<Country> displayAllNeighboursJGraphT(Country selectedCountry) {

		List<Country> visited = new LinkedList<Country>();
		
		// Versione 1: utilizzo un BreadthFirstIterator
//		GraphIterator<Country, DefaultEdge> dfv = new BreadthFirstIterator<Country, DefaultEdge>(grafo, selectedCountry);
//		
//		while(dfv.hasNext()) {
//			visited.add(dfv.next());
//		}
		
		// Versione 2: utilizzo DepthFirstIterator
		GraphIterator<Country, DefaultEdge> dfv = new DepthFirstIterator<Country, DefaultEdge>(grafo, selectedCountry);
		
		while(dfv.hasNext()) {
			visited.add(dfv.next());
		}
		
		return visited;
	}
	
	/*
	 * VERSIONE RICORSIVA
	 */
	private List<Country> displayAllNeighboursRecursive(Country selectedCountry) {

		List<Country> visited = new LinkedList<Country>();
		recursieVisit(selectedCountry, visited);
		return visited;
	}

	private void recursieVisit(Country n, List<Country> visited) {
		// Do always
		visited.add(n);
		
		// Cycle
		for(Country c : Graphs.neighborListOf(grafo, n)){
			// filter
			if(!visited.contains(c))
				recursieVisit(c, visited);
				// DO NOT REMOVE!! (no backtrack)
		}
		
	}

	
	
	
	
	
	
	
	
	
	
	
}
