import sys
import xml.etree.ElementTree as ET
import networkx as nx
import json

from configmanager import get_python_config, get_java_config
from graphtools  import get_scxml_states_and_transitions, create_graph, create_edge_list, get_states_dict, edge_obj
from codeparser import parse_transition_and_make_sets, parse_entry_actions, split_transitions_labels
from pathgen import create_paths, make_executable, build_tests




def get_paths(xmlfile, lang, target_fnam):
    init_state = "init"
    if (lang == '-j'):
        header, model_lang_map, test_lang_cfg = get_java_config(target_fnam)
    elif (lang == '-p'):
        header, model_lang_map, test_lang_cfg = get_python_config()


    with open('config/wrappers.json', 'r') as f:
        map_dict = json.load(f)
    with open('config/implementations.json', 'r') as f:
        init_map = json.load(f)
    with open('config/mocks_init.json', 'r') as f:
        mocks_init = json.load(f)

    test_header = test_lang_cfg['test_header']
    test_preamble = test_lang_cfg['test_preamble']
    test_postamble = test_lang_cfg['test_postamble']
    global_postamble = test_lang_cfg['global_postamble']
    xmlgraph = ET.parse(xmlfile)
    objects = dict()

    # get states and transitions sets
    states, transitions = get_scxml_states_and_transitions(xmlgraph)
    # create transition objects
    transmapping = split_transitions_labels(transitions)
    # parse transitions' code
    exec_code = parse_transition_and_make_sets(transmapping, map_dict, model_lang_map, init_map, objects)
    # parse entry actions code
    states = parse_entry_actions(states, map_dict, model_lang_map, init_map, objects)
    # build NetworkX MultiDiGraph
    graph = create_graph(states, transitions, exec_code)
    # hashmap where keys are triples (source state, target state, 0) and values are transition objects
    edgeattrs = nx.get_edge_attributes(graph, 'attr_dict')

    # hashmap where keys are triples (source state, target state, transition object) and values are booleans that mark if the transition has been visited
    edges = create_edge_list(edgeattrs)
    nodes = {nd: [] for nd in graph.nodes}
    named_states = get_states_dict(states, map_dict)
    paths = create_paths(edges, init_state, nodes)
    print('\n')
    print(str(len(paths)) + ' total paths')
    print_paths(paths)
    print('Handling executability...')
    paths = make_executable(paths, named_states, edges, mocks_init)
    print_paths(paths)
    print(str(len(paths)) + ' paths after executability process')
    print('Building tests')

    build_tests(paths, named_states, target_fnam, test_header, header, test_preamble, test_postamble, global_postamble)
    return paths, edges, init_state, named_states


def print_paths(paths):
    print('--------- START PATHS -------')
    path_idx = 0
    for path in paths:
        path_idx += 1
        print('\n\n------ Path '+str(path_idx))
        print_path(path)
    print('------- END PATHS ----------')


def print_path(path):
    for edge in path:
        einfo = edge_obj(edge[2])
        edgelabel = ''
        if 'event' in einfo and einfo['event']['type'] != 'empty':
            edgelabel += einfo['event']['code'] + ' '
        if 'guard' in einfo:
            edgelabel += '[ '
            for i in range(0, len(einfo['guard'])):
                if (einfo['guard'][i]['code'] != 'True'):
                    edgelabel += einfo['guard'][i]['code']
                    if (len(einfo['guard']) > 1 and i < len(einfo['guard']) - 1):
                        edgelabel += ' AND '
            edgelabel += ']'
        if 'action' in einfo:
            edgelabel += '/ '
            for a in einfo['action']:
                edgelabel += ' ' + a['code'] + ';'
        print(str(edge[0]) + ' --> ' + str(edge[1]) + ' ' + edgelabel)
        print('\n')


if __name__ == '__main__':
    get_paths(sys.argv[1], sys.argv[2], sys.argv[3])