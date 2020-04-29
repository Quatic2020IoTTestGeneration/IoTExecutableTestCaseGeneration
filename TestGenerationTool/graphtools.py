import json

import networkx as nx


def get_scxml_states_and_transitions(xmlgraph):
    states = xmlgraph.findall('.//{http://www.w3.org/2005/07/scxml}state')
    transitions = []
    for state in states:
        for t in state.getchildren():
            if t.tag == '{http://www.w3.org/2005/07/scxml}transition':
                t.set('source', state.attrib['name'])
                transitions.append(t)

    return (states, transitions)


def create_graph(states, transitions, trans_info):
    graph = nx.MultiDiGraph()
    graph.add_nodes_from([state.attrib['name'] for state in states])
    for t in transitions:
        print(trans_info[t.attrib['event']])
        graph.add_edge(t.attrib['source'], t.attrib['target'], attr_dict=trans_info[t.attrib['event']])
    return graph


def create_edge_list(edgeattrs):
    edges = dict()
    for edge in edgeattrs:
        print(edge)
        edges[(edge[0], edge[1], str(edgeattrs[edge]))] = False
    return edges


def get_states_dict(states, map_dict):
    named_states = dict()
    for state in states:
        named_states[state.attrib['name']] = state
    return named_states


def edge_obj(edge):
    return json.loads(edge.replace('\'', '\"'))