import json
import queue
import copy

from graphtools import edge_obj


def create_paths(edges, init_state, nodes):
    to_visit = queue.Queue()
    for edge in get_unvisited(init_state, edges):
        to_visit.put(edge)
    paths = []
    while (not to_visit.empty()):
        curredge = to_visit.get()
        print('Visiting ' + str(curredge))
        edges[curredge] = True
        source = curredge[0]
        target = curredge[1]
        if (len(nodes[target]) == 0):
            nodes[target] = list(nodes[source])
            nodes[target].append(curredge)
            curr_unvisited = get_unvisited(target, edges)
            print('curr_unvisited = ' + str(curr_unvisited))
            if (len(curr_unvisited) != 0):
                for edge in curr_unvisited:
                    to_visit.put(edge)
            else:
                print('!!! FINALIZING PATH (1)')
                print('\t > ' + str(nodes[target]))
                paths.append(nodes[target])
        else:
            currpath = list(nodes[source])
            currpath.append(curredge)
            print('!!! FINALIZING PATH (2)')
            print('\t > ' + str(currpath))
            paths.append(currpath)
    return paths


def get_unvisited(node, edges):
    unv = []
    for edge in edges:
        if (edge[0] == node and not edges[edge]):
            unv.append(edge)
    return unv


def get_outgoing(node, edges):
    out = []
    for edge in edges:
        if (edge[0] == node):
            out.append(edge)
    return out


def make_executable(paths, named_states, edges, mocks_init):
    res = []
    d = 0
    m = 0
    for path in paths:
        p = handle_executability(path, named_states, edges, mocks_init)
        if (p != []):
            res.append(p)
            if (str(p) != str(path)):
                m += 1
        else:
            d += 1
    print(str(d) + ' paths removed, ' + str(m) + ' paths modified')
    return res



def handle_executability(path, named_states, edges, mocks_init):

    newpath = []
    context = {}

    #import mocks
    for statement in mocks_init:
        exec(statement, {}, context)

    i = 0
    max_iter = 30
    for edge in path:
        edgeinfo = edge_obj(edge[2])
        if (not evaluate_guards(edgeinfo['guard'], context)):
            loop, ctx = generate_loop(edge, edges, max_iter, named_states, context)
            if(len(loop) != 0):
                context = ctx
                newpath += loop
        context = run_transition(context, edge, edgeinfo, named_states)
        newpath.append(edge)
        i += 1
    return newpath


def run_transition(context, edge, edgeinfo, named_states):
    if (edgeinfo['event']['type'] == 'assignment' or edgeinfo['event']['type'] == 'initialization' or edgeinfo['event']['type'] == 'call'):
        exec(edgeinfo['event']['code'], {}, context)
    if ('action' in edgeinfo):
        for a in edgeinfo['action']:
            if (a['type'] == 'assignment' or a['type'] == 'initialization' or a['type'] == 'call'):
                if('py_code' in a):
                    exec(a['py_code'], {}, context)
                else:
                    exec(a['code'], {}, context)
    # if target state has entry actions
    if (named_states[edge[1]].attrib['entryAction']):
        for ea in named_states[edge[1]].attrib['entryAction']:
            if (ea['type'] == 'assignment' or ea['type'] == 'initialization' or ea['type'] ==  'call'):
                # print('!!! RUNNING ENTRY ACTION ' + ea['code'])
                exec(ea['code'], {}, context)
    return context


def get_failed_guard(guards, context):
    res = []
    for i in range(0, len(guards)):
        if (not eval(guards[i]['code'], {}, context)):
            res.append(i)
    return res


def evaluate_guards(guards, context):
    for guard in guards:
        if(not eval(guard['code'], {}, context)):
            return False;
    return True;

def generate_loop(fail_edge, edges, max_iter, named_states, old_context):

    context = copy.deepcopy(old_context)

    loop = []
    i = 0
    curr_edge = fail_edge
    einfo = edge_obj(curr_edge[2])
    full_guard = einfo['guard']
    while i < max_iter:
        failed = get_failed_guard(full_guard, context)
        nextedge = get_next_edge(failed, full_guard, curr_edge, edges, named_states, copy.deepcopy(context))
        nextinfo = edge_obj(nextedge[2])
        context = run_transition(context, nextedge, nextinfo, named_states)
        loop.append(nextedge)
        curr_edge = nextedge

        # failing transition is now traversable, return the loop
        if (evaluate_guards(full_guard, context) and nextedge[1] == fail_edge[0]):
            return loop, context
        i += 1

    raise Exception('Unable to find loop')




def get_next_edge(failed_guards, full_guard, curr_edge, edges, named_states, context):
    outgoing = get_outgoing(curr_edge[0], edges)

    candidates = [x for x in outgoing if (
            evaluate_guards(edge_obj(x[2])['guard'], context))]
    if(len(candidates) > 0):

        bf_vals = [[] for c in candidates]

        #current branch function value for all guards
        curr_bf_vals = eval(full_guard[0]['branch_f']['func'], {}, context)
        i = 0
        for candidate in candidates:
            ctx = copy.deepcopy(context)
            curr_cand = edge_obj(candidate[2])
            ctx = run_transition(ctx, candidate, curr_cand, named_states)
            #compute the branch function value of the target guard after executing each candidate transition
            bf_vals[i] = eval(full_guard[0]['branch_f']['func'], {}, ctx)
            i+=1

        #return the transition that gave the minimum branch function value
        return candidates[bf_vals.index(min(bf_vals))]
    else:
        return None



def build_tests(paths, named_states, testfnam, testnamestr, glob_pre, pre, post, glob_post):
    testfile = open(testfnam, 'w')
    testfile.write(glob_pre)
    test_no = 0
    for path in paths:
        test_no += 1
        testfile.write(pre)
        testfile.write('\t' + testnamestr.format(test_no))
        for currtrans in path:
            code = json.loads(currtrans[2].replace('\'', '\"'))
            # run event
            if ('event' in code and code['event']['code'] != ''):
                testfile.write('\t\t' + code['event']['code'] + ';\n')
            # run assertions
            if ('action' in code):
                for a in code['action']:
                    testfile.write('\t\t' + a['code'] + ';\n')
            # run entry action of target state, if any
            for ea in named_states[currtrans[1]].attrib['entryAction']:
                testfile.write('\t\t' + ea['code'] + ';\n')
        testfile.write(post)
        testfile.write('\n')
    testfile.write(glob_post)
    testfile.close()