import openpyxl
import os
import random
import sys
import time

# ==============================================================================
# HOW TO RUN
# python src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generator/generate_massive_orgs_excel.py
# ==============================================================================

DEFAULT_OUTPUT_FILENAME = "organization-development-flow1-generated.xlsx"
DEFAULT_TARGET_ORG_COUNT = 15        # Silakan ubah angka di sini (misal: 5, 10, 15, 30)
DEFAULT_AUTO_POPULATE_PROCEED = True  # Set True (nama unik timestamp) atau False (nama statis)

def generate_orgs(output_filename=DEFAULT_OUTPUT_FILENAME, count=DEFAULT_TARGET_ORG_COUNT, auto_populate_proceed=DEFAULT_AUTO_POPULATE_PROCEED):
    current_dir = os.path.dirname(os.path.abspath(__file__))
    loader_dir = os.path.dirname(current_dir)
    massive_dir = os.path.dirname(loader_dir)
    data_dir = os.path.join(massive_dir, 'data')

    template_path = os.path.join(data_dir, 'organization-development-flow1.xlsx')
    output_path = os.path.join(current_dir, output_filename)

    # Load LOVs from data/ subfolder
    with open(os.path.join(data_dir, 'JobLOV.txt'), 'r', encoding='utf-8') as f:
        jobs = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'LocationLOV.txt'), 'r', encoding='utf-8') as f:
        locations = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'Rc_codeLOV.txt'), 'r', encoding='utf-8') as f:
        rc_codes = [line.strip() for line in f if line.strip()]

    # Master Topologic org data definitions (30 nodes D1 -> D6)
    all_org_data = [
        # Level D1 (2 orgs)
        ('D1', 'Transformation Strategy & Governance D1', 'Corporate Assurance ID', 'Chief Audit Executive'),
        ('D1', 'Core Technology & Engineering D1', 'Corporate Assurance ID', 'Chief Audit Executive'),

        # Level D2 (4 orgs)
        ('D2', 'Enterprise Architecture & Platform D2', 'Transformation Strategy & Governance D1', 'Pos Transformation Strategy & Governance D1'),
        ('D2', 'Quality Governance & Compliance D2', 'Transformation Strategy & Governance D1', 'Pos Transformation Strategy & Governance D1'),
        ('D2', 'Software Engineering & Delivery D2', 'Core Technology & Engineering D1', 'Pos Core Technology & Engineering D1'),
        ('D2', 'Cloud & Infrastructure Operations D2', 'Core Technology & Engineering D1', 'Pos Core Technology & Engineering D1'),

        # Level D3 (5 orgs)
        ('D3', 'Automation Testing & COE D3', 'Enterprise Architecture & Platform D2', 'Pos Enterprise Architecture & Platform D2'),
        ('D3', 'Performance & Security Assurance D3', 'Quality Governance & Compliance D2', 'Pos Quality Governance & Compliance D2'),
        ('D3', 'Digital Service Engineering D3', 'Software Engineering & Delivery D2', 'Pos Software Engineering & Delivery D2'),
        ('D3', 'API & Middleware Integration D3', 'Software Engineering & Delivery D2', 'Pos Software Engineering & Delivery D2'),
        ('D3', 'Site Reliability & Infrastructure D3', 'Cloud & Infrastructure Operations D2', 'Pos Cloud & Infrastructure Operations D2'),

        # Level D4 (6 orgs)
        ('D4', 'Core Automation Framework Unit D4', 'Automation Testing & COE D3', 'Pos Automation Testing & COE D3'),
        ('D4', 'Performance Engineering Unit D4', 'Performance & Security Assurance D3', 'Pos Performance & Security Assurance D3'),
        ('D4', 'Cyber Security Audit Unit D4', 'Performance & Security Assurance D3', 'Pos Performance & Security Assurance D3'),
        ('D4', 'Web Application Platform Unit D4', 'Digital Service Engineering D3', 'Pos Digital Service Engineering D3'),
        ('D4', 'Microservices Integration Unit D4', 'API & Middleware Integration D3', 'Pos API & Middleware Integration D3'),
        ('D4', 'Cloud Infrastructure SRE Unit D4', 'Site Reliability & Infrastructure D3', 'Pos Site Reliability & Infrastructure D3'),

        # Level D5 (6 orgs)
        ('D5', 'UI Framework Sub-Unit D5', 'Core Automation Framework Unit D4', 'Pos Core Automation Framework Unit D4'),
        ('D5', 'API Automation Sub-Unit D5', 'Core Automation Framework Unit D4', 'Pos Core Automation Framework Unit D4'),
        ('D5', 'Load & Stress Testing Sub-Unit D5', 'Performance Engineering Unit D4', 'Pos Performance Engineering Unit D4'),
        ('D5', 'Frontend Platform Sub-Unit D5', 'Web Application Platform Unit D4', 'Pos Web Application Platform Unit D4'),
        ('D5', 'DevOps Pipeline Sub-Unit D5', 'Microservices Integration Unit D4', 'Pos Microservices Integration Unit D4'),
        ('D5', 'Container Operations Sub-Unit D5', 'Cloud Infrastructure SRE Unit D4', 'Pos Cloud Infrastructure SRE Unit D4'),

        # Level D6 (7 orgs)
        ('D6', 'Web Automation Execution Squad D6', 'UI Framework Sub-Unit D5', 'Pos UI Framework Sub-Unit D5'),
        ('D6', 'Mobile Automation Execution Squad D6', 'UI Framework Sub-Unit D5', 'Pos UI Framework Sub-Unit D5'),
        ('D6', 'REST API Testing Squad D6', 'API Automation Sub-Unit D5', 'Pos API Automation Sub-Unit D5'),
        ('D6', 'Capacity Planning Squad D6', 'Load & Stress Testing Sub-Unit D5', 'Pos Load & Stress Testing Sub-Unit D5'),
        ('D6', 'Design System Squad D6', 'Frontend Platform Sub-Unit D5', 'Pos Frontend Platform Sub-Unit D5'),
        ('D6', 'Continuous Deployment Squad D6', 'DevOps Pipeline Sub-Unit D5', 'Pos DevOps Pipeline Sub-Unit D5'),
        ('D6', 'Kubernetes Platform Squad D6', 'Container Operations Sub-Unit D5', 'Pos Container Operations Sub-Unit D5'),
    ]

    target_count = min(max(1, count), len(all_org_data))
    org_data = all_org_data[:target_count]

    wb = openpyxl.load_workbook(template_path)
    sheet_org = wb['Organization']
    sheet_pos = wb['Position']

    # Keep static non-ADD_NEW rows from template
    org_rows_to_keep = [r for r in list(sheet_org.iter_rows(values_only=True)) if r[1] != 'ADD_NEW']
    sheet_org.delete_rows(1, sheet_org.max_row)
    for r in org_rows_to_keep:
        sheet_org.append(r)

    pos_rows_to_keep = [r for r in list(sheet_pos.iter_rows(values_only=True)) if not (r[1] and any(org_item[1] in str(r[1]) for org_item in all_org_data))]
    sheet_pos.delete_rows(1, sheet_pos.max_row)
    for r in pos_rows_to_keep:
        sheet_pos.append(r)

    start_no_org = len(org_rows_to_keep) - 4
    start_no_pos = len(pos_rows_to_keep) - 4

    run_seed = f"_{int(time.time() * 1000)}" if auto_populate_proceed else ""

    created_map = {}
    pos_seq_counter = 0

    for idx, (level, raw_org_name, raw_parent_org, raw_parent_pos) in enumerate(org_data, start=1):
        rc = rc_codes[(idx - 1) % len(rc_codes)]
        org_name = f"{raw_org_name}{run_seed}" if auto_populate_proceed else raw_org_name
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

            # Dynamic New Job Injection logic (~30% new job injection if auto_populate_proceed is True)
            if auto_populate_proceed and (pos_seq_counter % 3 == 0):
                job_name = f"Auto Created Job Title {pos_seq_counter}{run_seed}"
            else:
                job_name = jobs[(pos_seq_counter - 1) % len(jobs)]

            # Position Naming Logic
            if k == 1:
                raw_pos_name = f"Pos {raw_org_name}"
            else:
                raw_pos_name = f"Pos {raw_org_name} {k}"

            pos_name = f"{raw_pos_name}{run_seed}" if auto_populate_proceed else raw_pos_name

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
    print(f'Successfully generated {len(org_data)} Organizations with Multi-Position & New Job Injection into: {output_path}')

if __name__ == '__main__':
    out_file = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_OUTPUT_FILENAME
    limit = int(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_TARGET_ORG_COUNT
    auto_unique = sys.argv[3].lower() == 'true' if len(sys.argv) > 3 else DEFAULT_AUTO_POPULATE_PROCEED
    generate_orgs(output_filename=out_file, count=limit, auto_populate_proceed=auto_unique)
