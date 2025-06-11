import requests
import json
import requests_cache

# API endpoint URL
url = "https://api.srv.nat.tum.de"

def get_org_rec(org_id, added=None):
    if added is None:
        added = set()

    if org_id in added:
        return None

    added.add(org_id)

    response = requests.get(url + f"/api/v1/orgs/{org_id}")
    org = response.json()

    old_set = org.get("org_parents", [])

    org["org_parents"] = {
        parent_id: get_org_rec(parent_id, added)
        for parent_id in old_set
        if parent_id != 1
    }

    return org

def get_all(*args, **kwargs):
    items = []
    response = requests.get(*args, **kwargs).json()    
    items += response["hits"]

    total = response["total_count"]
    while "next_offset" in response and response["next_offset"] is not None:
        print(f"{len(items)}/{total}")
        if "params" in kwargs:
            kwargs["params"] |= {"offset": response["next_offset"]}
        else:
            kwargs["params"] = {"offset": response["next_offset"]}
        response = requests.get(*args, **kwargs).json()
        items += response["hits"]
    return items

try:
    requests_cache.install_cache("cache", expire_after=36000)

    # Make GET request
    semester_key = requests.get(url + "/api/v1/semesters/lecture").json()[
        "semester_key"
    ]
    print(f"semester: {semester_key}")
    print()
    
    headers = {
        'Accept': 'application/json'
    }

    curricula = requests.get("https://campus.tum.de/tumonline/ee/rest/slc.cm.cs.student/curricula/204", headers=headers).json()["resource"]

    query = input("Enter Study Program: ")

    for program in get_all(
        url + "/api/v1/programs/search",
        params = {
            "q": query
        }
    ):
        print(f"program: {program['degree_program_name']}")

        study_id = program["study_id"]
        print("study id:", study_id)

        org_id = program["org_id"]
        print("org id:", org_id)

        school_org_id = program["school"]["org_id"]
        print("school org id:", school_org_id)  # no need to go deeper

        spo_version = program["spo_version"]
        print("spo version:", spo_version)

        long_name = f"{program['program_name']} [{spo_version}], {program["degree"]["degree_type_name"]}"
        print("long name:", long_name)

        found_id = False
        for curriculum in curricula:
            curriculum = curriculum["content"]["cmCurriculumVersionDto"]
            if curriculum["name"]["value"] != long_name:
                continue

            curriculum_id = curriculum["id"]
            found_id = True

            break

        if not found_id:
            print("no curriculum available")
            print()
            continue
        
        print("curriculum id:", curriculum_id)
        print()

    courses = []
    
    term_id = 204

    params = {
        "$filter": f"courseNormKey-eq=LVEAB;curriculumVersionId-eq={curriculum_id};orgId-eq=1;termId-eq={term_id}",
        # "$orderBy": "title asc",  # Corrected orderby parameter
        "$skip": 0,
        "$top": 20
    }
    
    response = requests.get("https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student/courses", params=params, headers=headers).json()
    courses += response["courses"]
    
    print(response["totalCount"])

    while len(courses) < response["totalCount"]:
        params = {
            "$filter": f"courseNormKey-eq=LVEAB;curriculumVersionId-eq={curriculum_id};orgId-eq=1;termId-eq={term_id}",
            # "$orderBy": "title asc",  # Corrected orderby parameter
            "$skip": len(courses),
            "$top": len(courses)+20
        }
    
        response = requests.get("https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student/courses", params=params, headers=headers).json()
        courses += response["courses"]
        
        print(len(courses), response["totalCount"])

    for course in courses:
      # print(json.dumps(course, indent=4))
      # print(list(course.keys()))
      print(course["id"], course["courseTitle"]["value"])
      # print(course["semesterDto"])

        # !!!!!!!!!!!!!!!!!!! The course id can be used to join the apis !!!!!!!!!!!!!!!!!!!!!!

except requests.exceptions.RequestException as e:
    print(f"An error occurred: {e}")
