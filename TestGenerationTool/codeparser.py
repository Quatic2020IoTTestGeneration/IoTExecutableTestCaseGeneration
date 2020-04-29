import re

def parse_code(code, map_dict, lang_map, init_map, objects):
    regex = dict()
    regex['assignment'] = '(?P<target>\w+)\s*(?P<operator>\:=)\s*(?P<value>.+);?'
    regex['call'] = '(?P<actor>\w+)\.(?P<function>\w+)(?P<params>\(.*\));?'
    regex['func'] = '(?P<function>\w+)(?P<params>\(.*\));?'
    regex['bool_comp'] = '((?P<negation>not\s)?(?P<leftside>.+)\s*(?P<operator>\sAND\s|\sOR\s)\s*(?P<rightside>.+))'
    regex['bool_atom'] = '((?P<negation>not\s)?(?P<leftside>.+)\s*(?P<operator>\>\=|\<\=|\<|\>|\=\=)\s*(?P<rightside>.+))|True|False'

    regex['arit_op'] = '(?P<leftside>\w+)\s*(?P<operator>\+|\-|\*\/)\s*(?P<rightside>\w+);?'
    regex['var'] = '[a-zA-Z]+\w*'

    assignment_matcher = re.fullmatch(regex['assignment'], code.strip())
    call_matcher = re.fullmatch(regex['call'], code.strip())

    bool_comp_matcher = re.fullmatch(regex['bool_comp'], code.strip())
    bool_atom_matcher = re.fullmatch(regex['bool_atom'], code.strip())
    #if-elif order is meaningful
    #ALWAYS check bool_comp before bool_atom
    if (call_matcher):
        # extract parameters if any
        return parse_call(call_matcher, map_dict, code, regex, objects)
    elif (assignment_matcher):
        return parse_assignment(assignment_matcher, map_dict, code, regex, lang_map, init_map, objects)
    elif (bool_comp_matcher):
        return parse_bool_comp_exp(bool_comp_matcher, code, regex, lang_map)
    elif (bool_atom_matcher):
        return parse_bool_atom_exp(bool_atom_matcher, code, regex, lang_map)

    else:
        return {'type': 'empty', 'code': ''}


def parse_bool_atom_exp(bool_matcher, code, regex, lang_map):
    if(bool_matcher.group('leftside') and bool_matcher.group('rightside')):
        branch_f = get_branch_function(bool_matcher)
    else:
        branch_f = 'nobf'
    return {'type': 'cond', 'branch_f': branch_f, 'code': code.strip()}

def parse_bool_comp_exp(bool_matcher, code, regex, lang_map):
    comp_left_matcher = re.fullmatch(regex['bool_comp'], bool_matcher.group('leftside'))
    atom_left_matcher = re.fullmatch(regex['bool_atom'], bool_matcher.group('leftside'))
    comp_right_matcher = re.fullmatch(regex['bool_comp'], bool_matcher.group('rightside'))
    atom_right_matcher = re.fullmatch(regex['bool_atom'], bool_matcher.group('rightside'))

    if(comp_left_matcher):
        left = parse_bool_comp_exp(comp_left_matcher, bool_matcher.group('leftside'), regex, lang_map)
    elif(atom_left_matcher):
        left = parse_bool_atom_exp(atom_left_matcher, bool_matcher.group('leftside'), regex, lang_map)

    if(comp_right_matcher):
        right = parse_bool_comp_exp(comp_right_matcher, bool_matcher.group('rightside'), regex, lang_map)
    elif (atom_right_matcher):
        right = parse_bool_atom_exp(atom_right_matcher, bool_matcher.group('rightside'), regex, lang_map)

    bf = compose_branch_function(bool_matcher, left['branch_f']['func'], right['branch_f']['func'])
    code = left['code']+' '+lang_map[bool_matcher.group('operator').strip()]+' '+right['code']
    return {'type': 'cond', 'branch_f' : bf, 'code' : code}


#compute branch function for AND/OR operations
def compose_branch_function(bool_matcher, left, right):
    op = bool_matcher.group('operator').strip()
    bf = dict()
    if(op == 'AND'):
        bf['func'] = '('+left+') + ('+right+')'
    elif(op == 'OR'):
        bf['func'] = 'min(('+left+'), ('+right+'))'

    return bf


#compute branch function for basic relational operations
def get_branch_function(bool_matcher):
    op = bool_matcher.group('operator').strip()
    left = bool_matcher.group('leftside').strip()
    right = bool_matcher.group('rightside').strip()
    bf = dict()
    #branch predicate: E1 > E1
    #branch function: E2 - E1
    #relation: <
    if(op == '>'):
        #bf['rel'] = '<'
        bf['func'] = '0 if ('+left+') > ('+right+') else (('+right+') - ('+left+') + 5)'
    elif(op == '>='):
        #bf['rel'] = '<='
        bf['func'] = '0 if ('+left+') >= ('+right+') else (('+right + ') - (' + left+') + 5)'
    elif(op == '<'):
        #bf['rel'] = '<'
        bf['func'] = '0 if ('+left+') < ('+right+') else (('+left + ') - (' + right+') + 5)'
    elif(op == '<='):
        #bf['rel'] = '<='
        bf['func'] = '0 if ('+left+') <= ('+right+') else (('+left + ') - (' + right+') + 5)'
    elif(op == '=='):
        #bf['rel'] = '=='
        bf['func'] = '0 if ('+left+') == ('+right+') else abs('+left + ' - ' + right+') + 5'
    elif(op == '!='):
        #bf['rel'] = '<='
        bf['func'] = '0 if ('+left+') != ('+right+') else abs(' + left + ' - ' + right + ') + 5'
    else:
        raise Exception('Cannot compute branch function')

    return bf


def parse_assignment(assignment_matcher, map_dict, code, regex, lang_map, init_map, objects):
    oper = assignment_matcher.group('operator')
    rightside = assignment_matcher.group('value')
    opmatch = re.fullmatch(regex['arit_op'], rightside)
    funcmatch = re.fullmatch(regex['func'], rightside)
    if (opmatch):
        parse_arit_op(opmatch, regex)
    elif(funcmatch):
        impl_name = init_map[funcmatch.group('function')]
        impl_code = lang_map['new'].format(impl_name)
        objects[assignment_matcher.group('target')] = funcmatch.group('function')
        python_code = code.replace(funcmatch.group('function'), impl_name)
        code = code.replace(funcmatch.group('function'), impl_code)

        return {'type' : 'initialization',  'code': code.replace(oper, lang_map[oper]), 'py_code': python_code.replace(oper, lang_map[oper])}
    return {'type': 'assignment', 'code': code.replace(oper, lang_map[oper])}


def parse_arit_op(opmatch,  regex):
    right_exp = re.fullmatch(regex['arit_op'], opmatch.group('rightside'))
    if (right_exp):
        parse_arit_op(right_exp, regex)


def parse_call(call_matcher, map_dict, code, regex, objects):
    uml_params = call_matcher.group('params')
    uml_params = uml_params[1:-1]
    split_params = uml_params.split(',')
    # remove parameters to keep only event name
    m = re.search('\(.*\)', code)
    res = code[:m.start()] + code[m.end():]
    # get actual function name and additional parameters for current event
    varname = call_matcher.group('actor')
    clsname = objects[varname]
    curr_transform = map_dict[clsname][call_matcher.group('function')]
    res = varname+'.'+curr_transform['function'] + '(' + uml_params
    if (curr_transform['params'] != '' and uml_params != ''):
        res += ','
    res += (curr_transform['params'] + ')')
    return {'type': 'call',  'code': res}


def parse_transition_and_make_sets(transition_objs, map_dict, lang_map, init_map, objects):
    executable_code = dict()

    for t in transition_objs:
        exec_t = dict()
        curr_t = transition_objs[t]
        print('Transforming ' + t)
        if ('event' in curr_t):
            exec_t['event'] = parse_code(curr_t['event'], map_dict, lang_map, init_map, objects)

        if ('action' in curr_t):
            exec_t['action'] = []
            actions = curr_t['action'].split(';')
            for action in actions:
                if (action != ''):
                    action = action.strip()
                    action = action.replace('\n', '')
                    parsedact = parse_code(action, map_dict, lang_map, init_map, objects)
                    exec_t['action'].append(parsedact)
        if ('guard' in curr_t):
            exec_t['guard'] = []
            # remove spaces and square brackets around guards
            grds = curr_t['guard'].strip()[1:-1]
            guards = [grds]
            for i in range(0, len(guards)):
                if (guards[i] != ''):
                    curr_guard = parse_code(guards[i], map_dict, lang_map, init_map, objects)
                    exec_t['guard'].append(curr_guard)
        executable_code[t] = exec_t

    return executable_code


def parse_entry_actions(states, map_dict, lang_map, init_map, objects):
    for i in range(0, len(states)):
        actions = states[i].attrib['entryAction'].split(';')
        parsed = []
        #print('PRE PARSING: ' + str(actions))
        for action in actions:
            if (action != ''):
                action = action.strip()
                action = action.replace('\n', '')
                parsed.append(parse_code(action, map_dict, lang_map, init_map, objects))
        #print('POST PARSING: ' + str(parsed))
        states[i].attrib['entryAction'] = parsed
    return states


def split_transitions_labels(transitions):
    parsed = dict()
    for t in transitions:

        action = None
        guard = None
        curr = dict()
        tparts = t.attrib['event'].split('/')
        event = tparts[0].strip()
        if (len(tparts) == 2):  # transition has an action associated
            curr['action'] = tparts[1].strip()

        guard = re.findall('\[.*\]', event)

        # transition has a guard
        if (len(guard) > 0):
            curr['guard'] = guard[0]
            m = re.search('\[.*\]', event)
            event = event[:m.start()] + event[m.end():]
        else:
            curr['guard'] = '[True]'
        curr['event'] = event
        parsed[t.attrib['event']] = curr

    return parsed


def get_vars(params, var_regex):
    res = []
    for par in params:
        par = par.strip()
        varmatcher = re.search(var_regex, par)
        if (varmatcher):
            res.append(varmatcher.group())
    return res