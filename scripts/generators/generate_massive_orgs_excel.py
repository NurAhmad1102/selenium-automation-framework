import openpyxl
import os
import random
import sys
import time

# ==============================================================================
# HOW TO RUN
# python src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generator/generate_massive_orgs_excel.py [out_file] [limit] [auto_unique] [new_job_percentage]
# ==============================================================================

DEFAULT_OUTPUT_FILENAME = "organization-development-flow1-generated.xlsx"
DEFAULT_TARGET_ORG_COUNT = 100        # Silakan ubah angka di sini (misal: 5, 10, 15, 30, 50, 100)
DEFAULT_AUTO_POPULATE_PROCEED = True  # True (nama unik timestamp) atau False (nama statis)
DEFAULT_NEW_JOB_PERCENTAGE = 30       # Persentase posisi yang berupa New Job (0 s/d 100%)

SENIORITY_PREFIXES = [
    "Lead", "Senior", "Principal", "Staff", "Head of", "Chief Specialist",
    "Executive Specialist", "Associate Director", "Senior Lead", "Consulting Lead"
]

DOMAIN_DISCIPLINES = [
    "Cloud Infrastructure & SRE", "Cyber Security Assurance", "Core Banking Integration",
    "Microservices & API Platform", "Automated QA & COE", "Data Governance & Analytics",
    "DevSecOps Pipeline", "Enterprise Architecture", "Digital Channels & Mobile",
    "AI & Machine Learning Engineering", "Risk & Compliance Audit", "Platform Reliability & Systems",
    "Financial Technology Solutions", "Business Process Automation", "Database Administration & Ops",
    "Identity & Access Management", "Payment Gateway Solutions", "Treasury Systems & Ops"
]

ROLE_SUFFIXES = [
    "Engineer", "Specialist", "Architect", "Assurance Officer", "Strategist",
    "Consultant", "Analyst", "Manager"
]

def generate_realistic_job_title(seq, run_seed=""):
    seniority = SENIORITY_PREFIXES[(seq - 1) % len(SENIORITY_PREFIXES)]
    domain = DOMAIN_DISCIPLINES[(seq - 1) % len(DOMAIN_DISCIPLINES)]
    role = ROLE_SUFFIXES[(seq - 1) % len(ROLE_SUFFIXES)]
    base_title = f"{seniority} {domain} {role}"
    return f"{base_title}{run_seed}".strip() if run_seed else base_title

def is_new_job_by_rate(seq_counter, percentage):
    if percentage <= 0:
        return False
    if percentage >= 100:
        return True
    return (seq_counter * percentage // 100) > ((seq_counter - 1) * percentage // 100)

def build_dynamic_org_hierarchy(depts, target_count, d0_org_name, d0_pos_name):
    """
    Builds a list of N organization specs across levels D1 to D6 connected in a valid tree.
    Each item in returned list is a tuple: (level, raw_org_name, raw_parent_org, raw_parent_pos)
    """
    if not depts:
        depts = ["Transformation Office", "Core Technology", "Enterprise Architecture", "Quality Governance",
                 "Software Engineering", "Cloud Operations", "Automation COE", "Security Assurance"]

    target = max(1, target_count)

    # Determine level distribution (D1 to D6) with exact sum guarantee
    if target <= 6:
        level_counts = [1 if i < target else 0 for i in range(6)]
    else:
        weights = [0.08, 0.14, 0.20, 0.22, 0.18, 0.18]
        level_counts = [1, 1, 1, 1, 1, 1]
        rem = target - 6
        alloc = [int(rem * w) for w in weights]
        for i in range(6):
            level_counts[i] += alloc[i]
        leftover = target - sum(level_counts)
        priority_order = [3, 2, 4, 1, 5, 0] # D4, D3, D5, D2, D6, D1
        for p in priority_order:
            if leftover <= 0:
                break
            level_counts[p] += 1
            leftover -= 1

    level_names = ['D1', 'D2', 'D3', 'D4', 'D5', 'D6']
    created_by_level = {lvl: [] for lvl in level_names}
    all_nodes = []
    node_seq = 0

    for lvl_idx, lvl in enumerate(level_names):
        count_for_lvl = level_counts[lvl_idx]
        for _ in range(count_for_lvl):
            node_seq += 1
            dept_idx = (node_seq - 1) % len(depts)
            dept_cycle = ((node_seq - 1) // len(depts)) + 1
            dept_name = depts[dept_idx]

            if dept_cycle == 1:
                raw_org_name = f"{dept_name} {lvl}"
            else:
                raw_org_name = f"{dept_name} {lvl} {dept_cycle}"

            raw_pos_name = f"Pos {raw_org_name}"

            if lvl == 'D1':
                raw_parent_org = d0_org_name
                raw_parent_pos = d0_pos_name
            else:
                parent_level = level_names[lvl_idx - 1]
                parent_pool = created_by_level[parent_level]
                if not parent_pool:
                    raw_parent_org = d0_org_name
                    raw_parent_pos = d0_pos_name
                else:
                    chosen_parent = parent_pool[len(created_by_level[lvl]) % len(parent_pool)]
                    raw_parent_org = chosen_parent['raw_org_name']
                    raw_parent_pos = chosen_parent['raw_pos_name']

            node_info = {
                'level': lvl,
                'raw_org_name': raw_org_name,
                'raw_pos_name': raw_pos_name,
                'raw_parent_org': raw_parent_org,
                'raw_parent_pos': raw_parent_pos
            }
            created_by_level[lvl].append(node_info)
            all_nodes.append((lvl, raw_org_name, raw_parent_org, raw_parent_pos))

    return all_nodes

def generate_orgs(output_filename=DEFAULT_OUTPUT_FILENAME,
                  count=DEFAULT_TARGET_ORG_COUNT,
                  auto_populate_proceed=DEFAULT_AUTO_POPULATE_PROCEED,
                  new_job_percentage=DEFAULT_NEW_JOB_PERCENTAGE):
    current_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(os.path.dirname(current_dir))
    data_dir = os.path.join(project_root, 'src', 'test', 'resources', 'data', 'frontoffice', 'manageOrganization', 'massiveChanges')
    if not os.path.exists(data_dir):
        data_dir = os.path.join(project_root, 'src', 'test', 'resources', 'data', 'frontoffice', 'massiveChanges')

    template_path = os.path.join(data_dir, 'organization-development-flow1.xlsx')

    if os.path.isabs(output_filename):
        output_path = output_filename
    elif os.path.sep in output_filename or '/' in output_filename:
        output_path = os.path.abspath(output_filename)
    else:
        output_path = os.path.join(data_dir, output_filename)

    # Load LOVs from data/ subfolder
    with open(os.path.join(data_dir, 'DeptLOV.txt'), 'r', encoding='utf-8') as f:
        depts = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'JobLOV.txt'), 'r', encoding='utf-8') as f:
        jobs = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'LocationLOV.txt'), 'r', encoding='utf-8') as f:
        locations = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'Rc_codeLOV.txt'), 'r', encoding='utf-8') as f:
        rc_codes = [line.strip() for line in f if line.strip()]

    wb = openpyxl.load_workbook(template_path)
    sheet_org = wb['Organization']
    sheet_pos = wb['Position']

    # 1. Extract D0 Organization Name from sheet_org (Scanning row where Current Organization Level == 'D0')
    d0_org_name = None
    for row in list(sheet_org.iter_rows(values_only=True))[4:]:
        level = str(row[2]).strip() if row[2] is not None else ""
        if level.upper() == 'D0':
            d0_org_name = str(row[3]).strip() if row[3] is not None else ""
            break

    if not d0_org_name:
        raise ValueError(f"D0 Organization not found in template sheet 'Organization' (Level 'D0' is missing) in: {template_path}")

    # 2. Extract D0 Primary Position Name from sheet_pos (Scanning row where Organization Name == d0_org_name)
    d0_pos_name = None
    for row in list(sheet_pos.iter_rows(values_only=True))[4:]:
        org_name = str(row[1]).strip() if row[1] is not None else ""
        if org_name == d0_org_name:
            pos_name = str(row[3]).strip() if row[3] is not None else ""
            if pos_name:
                d0_pos_name = pos_name
                break

    if not d0_pos_name:
        raise ValueError(f"Primary position for D0 Organization '{d0_org_name}' not found in template sheet 'Position' in: {template_path}")

    # Dynamic Topologic org data definitions from DeptLOV.txt
    org_data = build_dynamic_org_hierarchy(depts, count, d0_org_name, d0_pos_name)

    # Keep static non-ADD_NEW rows from template
    org_rows_to_keep = [r for r in list(sheet_org.iter_rows(values_only=True)) if r[1] != 'ADD_NEW']
    sheet_org.delete_rows(1, sheet_org.max_row)
    for r in org_rows_to_keep:
        sheet_org.append(r)

    existing_org_names = set(str(r[3]).strip() for r in org_rows_to_keep[4:] if r[3])
    pos_all_rows = list(sheet_pos.iter_rows(values_only=True))
    pos_header_rows = pos_all_rows[:4]
    pos_data_to_keep = [r for r in pos_all_rows[4:] if r[1] and str(r[1]).strip() in existing_org_names]
    pos_rows_to_keep = pos_header_rows + pos_data_to_keep

    sheet_pos.delete_rows(1, sheet_pos.max_row)
    for r in pos_rows_to_keep:
        sheet_pos.append(r)

    start_no_org = len(org_rows_to_keep) - 4
    start_no_pos = len(pos_rows_to_keep) - 4

    run_seed = f" {int(time.time() * 1000)}" if auto_populate_proceed else ""

    created_map = {}
    pos_seq_counter = 0
    new_job_counter = 0

    for idx, (level, raw_org_name, raw_parent_org, raw_parent_pos) in enumerate(org_data, start=1):
        rc = rc_codes[(idx - 1) % len(rc_codes)]
        org_name = f"{raw_org_name}{run_seed}".strip() if auto_populate_proceed else raw_org_name
        parent_org = created_map.get(raw_parent_org, raw_parent_org)
        parent_pos = created_map.get(raw_parent_pos, raw_parent_pos)

        if auto_populate_proceed:
            created_map[raw_org_name] = org_name

        # Append Organization Row
        org_row = (start_no_org + idx, 'ADD_NEW', None, None, None, None, level, org_name, parent_org, rc)
        sheet_org.append(org_row)

        # Determine number of positions: D0-D2 = 1 position, D3-D6 = 2 to 6 positions
        if level in ['D0', 'D1', 'D2']:
            num_positions = 1
        else:
            num_positions = random.randint(2, 6)

        created_positions_in_org = []

        for k in range(1, num_positions + 1):
            pos_seq_counter += 1
            loc = locations[(pos_seq_counter - 1) % len(locations)]
            fte = (pos_seq_counter % 4) + 1

            # Dynamic New Job Injection logic based on configurable percentage
            if auto_populate_proceed and is_new_job_by_rate(pos_seq_counter, new_job_percentage):
                new_job_counter += 1
                job_name = generate_realistic_job_title(pos_seq_counter, run_seed)
            else:
                job_name = jobs[(pos_seq_counter - 1) % len(jobs)]

            # Position Naming Logic
            if k == 1:
                raw_pos_name = f"Pos {raw_org_name}"
            else:
                raw_pos_name = f"Pos {raw_org_name} {k}"

            pos_name = f"{raw_pos_name}{run_seed}".strip() if auto_populate_proceed else raw_pos_name

            if k == 1:
                # Primary Head Position reports to Parent Org's position
                curr_parent_pos = parent_pos
                if auto_populate_proceed:
                    created_map[raw_pos_name] = pos_name
            else:
                # Subordinate Position: Candidate Intra-Org Reporting (if Job differs) vs Inter-Org
                intra_candidates = [p for p in created_positions_in_org if p['job'] != job_name]
                if intra_candidates and random.choice([True, False]):
                    curr_parent_pos = intra_candidates[0]['pos_name'] # Intra-Org reporting
                else:
                    curr_parent_pos = parent_pos # Inter-Org reporting

            created_positions_in_org.append({'job': job_name, 'pos_name': pos_name})

            pos_row = (start_no_pos + pos_seq_counter, org_name, job_name, pos_name, curr_parent_pos, loc, fte, None)
            sheet_pos.append(pos_row)

    wb.save(output_path)
    pct_actual = (new_job_counter / pos_seq_counter * 100) if pos_seq_counter > 0 else 0
    print(f"Successfully generated {len(org_data)} Organizations and {pos_seq_counter} Positions ({new_job_counter} New Jobs, {pct_actual:.1f}%) under D0 Parent '{d0_org_name}' into: {output_path}")

if __name__ == '__main__':
    out_file = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_OUTPUT_FILENAME
    limit = int(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_TARGET_ORG_COUNT
    auto_unique = sys.argv[3].lower() == 'true' if len(sys.argv) > 3 else DEFAULT_AUTO_POPULATE_PROCEED
    new_job_pct = int(sys.argv[4]) if len(sys.argv) > 4 else DEFAULT_NEW_JOB_PERCENTAGE
    generate_orgs(output_filename=out_file, count=limit, auto_populate_proceed=auto_unique, new_job_percentage=new_job_pct)
