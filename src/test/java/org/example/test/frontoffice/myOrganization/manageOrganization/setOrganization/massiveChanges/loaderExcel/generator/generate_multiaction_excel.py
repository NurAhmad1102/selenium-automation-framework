import openpyxl
import os
import random
import sys
import time

# ==============================================================================
# HOW TO RUN:
# python src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generator/generate_multiaction_excel.py [out_file] [auto_unique] [new_job_percentage]
# ==============================================================================

DEFAULT_OUTPUT_FILENAME = "organization-development-flow1-multiaction.xlsx"
DEFAULT_AUTO_POPULATE_PROCEED = True  # True: dynamic unique timestamp suffix; False: static deterministic names
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
    return f"{base_title}{run_seed}" if run_seed else base_title

def is_new_job_by_rate(seq_counter, percentage):
    if percentage <= 0:
        return False
    if percentage >= 100:
        return True
    return (seq_counter * percentage // 100) > ((seq_counter - 1) * percentage // 100)

def generate_multiaction_excel(output_filename=DEFAULT_OUTPUT_FILENAME,
                               auto_populate_proceed=DEFAULT_AUTO_POPULATE_PROCEED,
                               new_job_percentage=DEFAULT_NEW_JOB_PERCENTAGE):
    current_dir = os.path.dirname(os.path.abspath(__file__))
    loader_dir = os.path.dirname(current_dir)
    massive_dir = os.path.dirname(loader_dir)
    data_dir = os.path.join(massive_dir, 'data')

    template_path = os.path.join(loader_dir, 'MultiAction', 'organization-development-flow1-3493.xlsx')
    if not os.path.exists(template_path):
        template_path = os.path.join(data_dir, 'organization-development-flow1.xlsx')

    if os.path.isabs(output_filename):
        output_path = output_filename
    elif os.path.sep in output_filename or '/' in output_filename:
        output_path = os.path.abspath(output_filename)
    else:
        output_path = os.path.join(current_dir, output_filename)

    # Load LOVs from data/ folder
    with open(os.path.join(data_dir, 'DeptLOV.txt'), 'r', encoding='utf-8') as f:
        depts = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'JobLOV.txt'), 'r', encoding='utf-8') as f:
        jobs = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'LocationLOV.txt'), 'r', encoding='utf-8') as f:
        locations = [line.strip() for line in f if line.strip()]

    with open(os.path.join(data_dir, 'Rc_codeLOV.txt'), 'r', encoding='utf-8') as f:
        rc_codes = [line.strip() for line in f if line.strip()]

    run_seed = f"_{int(time.time() * 1000)}" if auto_populate_proceed else ""

    wb = openpyxl.load_workbook(template_path)
    sheet_org = wb['Organization']
    sheet_pos = wb['Position']

    # Clean up any previously appended mutation rows (rows > 55) in template
    if sheet_org.max_row > 55:
        sheet_org.delete_rows(56, sheet_org.max_row - 55)

    # Map existing organizations and positions for side-effect tracking and initialize clean action state
    org_row_map = {}
    for r in range(6, sheet_org.max_row + 1):
        org_name = sheet_org.cell(r, 4).value
        if org_name:
            org_row_map[str(org_name).strip()] = r
            sheet_org.cell(r, 2).value = "NO_CHANGE"

    pos_mutation_map = {}
    global_pos_counter = 0

    # Define target Action Actors (from existing template nodes)
    remove_target = "O723 IT Audit - Tech Assurance Infrastructure & Cyber Security D3 ID"
    rename_target = "1223 IT Audit Advisory - Tech Infrastructure ID"
    merge_target_1 = "O723 IT Audit Advisory - Business Application ID"
    merge_target_2 = "O723 IT Audit Advisory - Cyber Security ID"
    split_target = "O723 IT Audit Advisory - Tech Innovation, Emerging Tech & QA ID"
    move_target = "Audit - QA & Audit Methodology ID"
    add_parent_target = "1023 Audit - Business Banking & Consumer Banking ID"

    default_loc = "Kantor Fungsional Non Operasional - Jakarta Synergy Buildinx"
    default_rc = "1611.47077.8504"

    def get_job_name(idx):
        if auto_populate_proceed and is_new_job_by_rate(idx, new_job_percentage):
            return generate_realistic_job_title(idx, run_seed)
        return jobs[idx % len(jobs)]

    def generate_positions_for_org(org_name, org_level, primary_parent_pos):
        nonlocal global_pos_counter
        # Determine number of positions: D0-D2 = 1 position, D3-D6 = 2 to 6 positions
        if org_level in ['D0', 'D1', 'D2']:
            num_pos = 1
        else:
            num_pos = random.randint(2, 6)

        created_in_org = []
        for k in range(1, num_pos + 1):
            global_pos_counter += 1
            job_n = get_job_name(global_pos_counter)
            pos_n = f"New Pos {k} {org_name}"
            loc_n = locations[global_pos_counter % len(locations)]
            fte_val = (global_pos_counter % 4) + 1.0

            if k == 1:
                p_pos = primary_parent_pos
            else:
                intra_candidates = [p for p in created_in_org if p['job'] != job_n]
                if intra_candidates and random.choice([True, False]):
                    p_pos = intra_candidates[0]['pos_name']
                else:
                    p_pos = primary_parent_pos

            created_in_org.append({'job': job_n, 'pos_name': pos_n})
            sheet_pos.append((sheet_pos.max_row - 4, org_name, job_n, pos_n, p_pos, loc_n, fte_val, ""))

    # =========================================================================
    # 1. ACTION: REMOVE
    # =========================================================================
    if remove_target in org_row_map:
        r = org_row_map[remove_target]
        sheet_org.cell(r, 2).value = "REMOVE"
        for c in range(7, 11):
            sheet_org.cell(r, c).value = ""
        print(f"[ACTION] REMOVE applied to row {r}: '{remove_target}'")

    # =========================================================================
    # 2. ACTION: RENAME
    # =========================================================================
    new_rename_org = f"New Rename ORG D3{run_seed}"
    new_rename_pos = f"New Pos 1 {new_rename_org}"
    if rename_target in org_row_map:
        r = org_row_map[rename_target]
        current_level = sheet_org.cell(r, 3).value or "D3"
        current_parent = sheet_org.cell(r, 5).value
        sheet_org.cell(r, 2).value = "RENAME"
        sheet_org.cell(r, 7).value = current_level
        sheet_org.cell(r, 8).value = new_rename_org
        sheet_org.cell(r, 9).value = current_parent
        sheet_org.cell(r, 10).value = rc_codes[1 % len(rc_codes)]

        old_pos_name = "IT Audit Advisory Sr Specialist - Tech Infrastructure 1223"
        pos_mutation_map[old_pos_name] = new_rename_pos

        parent_pos_for_rename = "IT Audit Advisory Head - Tech Innovation, Emerging Tech, QA & COE O723"
        generate_positions_for_org(new_rename_org, current_level, parent_pos_for_rename)
        print(f"[ACTION] RENAME applied to row {r}: '{rename_target}' -> '{new_rename_org}'")

    # =========================================================================
    # 3. ACTION: MERGE (2 Rows point to Same Merged Org)
    # =========================================================================
    new_merge_org = f"New Merge ORG D3{run_seed}"
    parent_pos_for_merge = "IT Audit Advisory Head - Tech Innovation, Emerging Tech, QA & COE O723"

    for merge_tgt in [merge_target_1, merge_target_2]:
        if merge_tgt in org_row_map:
            r = org_row_map[merge_tgt]
            current_level = sheet_org.cell(r, 3).value or "D3"
            current_parent = sheet_org.cell(r, 5).value
            sheet_org.cell(r, 2).value = "MERGE"
            sheet_org.cell(r, 7).value = current_level
            sheet_org.cell(r, 8).value = new_merge_org
            sheet_org.cell(r, 9).value = current_parent
            sheet_org.cell(r, 10).value = default_rc
            print(f"[ACTION] MERGE applied to row {r}: '{merge_tgt}' -> '{new_merge_org}'")

    generate_positions_for_org(new_merge_org, "D3", parent_pos_for_merge)

    # =========================================================================
    # 4. ACTION: SPLIT (Row 1 modifies existing, Row 2 appended)
    # =========================================================================
    new_split_org1 = f"New Split ORG 1 D3{run_seed}"
    new_split_org2 = f"New Split ORG 2 D3{run_seed}"
    parent_pos_for_split = "IT Audit Advisory Head - Tech Innovation, Emerging Tech, QA & COE O723"

    if split_target in org_row_map:
        r = org_row_map[split_target]
        cur_lvl = sheet_org.cell(r, 3).value or "D3"
        cur_org = sheet_org.cell(r, 4).value
        cur_parent = sheet_org.cell(r, 5).value
        cur_rc = sheet_org.cell(r, 6).value

        sheet_org.cell(r, 2).value = "SPLIT"
        sheet_org.cell(r, 7).value = cur_lvl
        sheet_org.cell(r, 8).value = new_split_org1
        sheet_org.cell(r, 9).value = cur_parent
        sheet_org.cell(r, 10).value = cur_rc

        sheet_org.append((sheet_org.max_row - 4, "SPLIT", cur_lvl, cur_org, cur_parent, cur_rc, cur_lvl, new_split_org2, cur_parent, cur_rc))

        generate_positions_for_org(new_split_org1, cur_lvl, parent_pos_for_split)
        generate_positions_for_org(new_split_org2, cur_lvl, parent_pos_for_split)
        print(f"[ACTION] SPLIT applied to row {r}: '{split_target}' -> '{new_split_org1}' & '{new_split_org2}'")

    # =========================================================================
    # 5. ACTION: MOVE (Move D3 to D1 under Corporate Assurance ID)
    # =========================================================================
    new_move_org = f"New Move ORG D1{run_seed}"
    parent_pos_for_move = "Chief Audit Executive"
    new_parent_for_move = "Corporate Assurance ID"

    if move_target in org_row_map:
        r = org_row_map[move_target]
        sheet_org.cell(r, 2).value = "MOVE"
        sheet_org.cell(r, 7).value = "D1"
        sheet_org.cell(r, 8).value = new_move_org
        sheet_org.cell(r, 9).value = new_parent_for_move
        sheet_org.cell(r, 10).value = rc_codes[0 % len(rc_codes)]

        generate_positions_for_org(new_move_org, "D1", parent_pos_for_move)
        print(f"[ACTION] MOVE applied to row {r}: '{move_target}' -> '{new_move_org}' under '{new_parent_for_move}'")

    # =========================================================================
    # 6. ACTION: ADD_NEW
    # =========================================================================
    new_add_org = f"New Add ORG D2{run_seed}"
    parent_pos_for_add = "Head of Audit - Business Banking & Consumer Banking 1023"

    sheet_org.append((sheet_org.max_row - 4, "ADD_NEW", None, None, None, None, "D2", new_add_org, add_parent_target, rc_codes[2 % len(rc_codes)]))
    generate_positions_for_org(new_add_org, "D2", parent_pos_for_add)
    print(f"[ACTION] ADD_NEW appended: '{new_add_org}' under '{add_parent_target}'")

    # =========================================================================
    # 7. SIDE EFFECT RESOLUTION: Adjust child Parent Position references
    # =========================================================================
    side_effect_count = 0
    for pr in range(6, sheet_pos.max_row + 1):
        parent_pos_val = sheet_pos.cell(pr, 5).value
        if parent_pos_val and str(parent_pos_val).strip() in pos_mutation_map:
            updated_pos = pos_mutation_map[str(parent_pos_val).strip()]
            sheet_pos.cell(pr, 5).value = updated_pos
            side_effect_count += 1
            print(f"[SIDE-EFFECT] Adjusted row {pr} Parent Position: '{parent_pos_val}' -> '{updated_pos}'")

    print(f"[INFO] Total side-effect parent positions adjusted: {side_effect_count}")

    wb.save(output_path)
    print(f"\n>>> Successfully generated MultiAction Excel: {output_path} <<<")
    return output_path

if __name__ == '__main__':
    out_file = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_OUTPUT_FILENAME
    unique = sys.argv[2].lower() == 'true' if len(sys.argv) > 2 else DEFAULT_AUTO_POPULATE_PROCEED
    new_job_pct = int(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_NEW_JOB_PERCENTAGE
    generate_multiaction_excel(output_filename=out_file, auto_populate_proceed=unique, new_job_percentage=new_job_pct)

