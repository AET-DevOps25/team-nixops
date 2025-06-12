import requests
import json
import requests_cache

# API endpoint URL
url = "https://api.srv.nat.tum.de"

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
    requests_cache.install_cache("cache", expire_after=None)

    # Make GET request
    semester = requests.get(url + "/api/v1/semesters/lecture").json()

    semester_key = semester["semester_key"]
    semester_id = semester["semester_id_tumonline"]
    semester_tag = semester["semester_tag"]
    semester_title = semester["semester_title"]
    
    print("Semester:")
    print("semester title:", semester_title)
    print("semester tag:", semester_tag)
    print("semester key:", semester_key)
    print("semester id:", semester_id)
    print()


    headers = {"Accept": "application/json"}

    curricula = requests.get(
        "https://campus.tum.de/tumonline/ee/rest/slc.cm.cs.student/curricula/204",
        headers=headers,
    ).json()["resource"]

    # query = input("Enter Study Program: ")
    # spo = input("Enter Id: ")

    query = "M.Sc. Informatik"
    spo = "20231"

    print("Study Program:")
    for program in get_all(url + "/api/v1/programs/search", params={"q": query}):
        print(f"program: {program['degree_program_name']}")

        study_id = program["study_id"]
        print("study id:", study_id)

        org_id = program["org_id"]
        print("org id:", org_id)

        school_org_id = program["school"]["org_id"]
        print("school org id:", school_org_id)  # no need to go deeper

        spo_version = program["spo_version"]
        print("spo version:", spo_version)

        if spo_version != spo:
            print("wrong spo")
            print()
            continue

        long_name = f"{program['program_name']} [{spo_version}], {program['degree']['degree_type_name']}"
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

    print("Fetch Modules:")
    modules = []
    response = requests.get(
        url + "/api/v1/mhb/module", params={"org_id": school_org_id}
    ).json()
    modules += response["hits"]

    total = response["total_count"]
    while "next_offset" in response and response["next_offset"] is not None:
        print(f"{len(modules)}/{response['total_count']}")
        response = requests.get(
            url + "/api/v1/mhb/module",
            params={"org_id": school_org_id, "offset": response["next_offset"]},
        ).json()
        modules += response["hits"]
    print()

    print("Modules:")
    extra_module_mapping = {}
    for module in modules:
        module_id = module["module_id"]
        module_title = module["module_title"]
        module_code = module["module_code"]

        # print(module_id, module_title, module_code)

        module = requests.get(url + f"/api/v1/mhb/module/{module_code}").json()

        del module["exams"]
        if semester_key in module["courses"]:
            for course in module["courses"][semester_key]:
                course_id = course["course_id"]
                # print(">>>", course_id)

                if course_id not in extra_module_mapping:
                    extra_module_mapping[course_id] = []

                extra_module_mapping[course_id].append(module_id)
    print()

    print("Fetch Courses:")
    courses = []

    params = {
        "$filter": f"courseNormKey-eq=LVEAB;curriculumVersionId-eq={curriculum_id};orgId-eq=1;termId-eq={semester_id}",
        # "$orderBy": "title asc",  # Corrected orderby parameter
        "$skip": 0,
        "$top": 20,
    }
    response = requests.get(
        "https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student/courses",
        params=params,
        headers=headers,
    ).json()
    courses += response["courses"]

    while len(courses) < response["totalCount"]:
        params = {
            "$filter": f"courseNormKey-eq=LVEAB;curriculumVersionId-eq={curriculum_id};orgId-eq=1;termId-eq={semester_id}",
            # "$orderBy": "title asc",  # Corrected orderby parameter
            "$skip": len(courses),
            "$top": len(courses) + 20,
        }

        response = requests.get(
            "https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student/courses",
            params=params,
            headers=headers,
        ).json()
        courses += response["courses"]
        print(len(courses), response["totalCount"])
    print()

    print("Courses:")
    for course in courses:
        # print(json.dumps(course, indent=4))
        # print(list(course.keys()))
        course_id = course["id"]
        # print(course["semesterDto"])
        new_course = requests.get(url + f"/api/v1/course/{course_id}").json()

        # print(course_id, course["courseTitle"]["value"])

        # if "Operations Research" in course["courseTitle"]["value"]:

        # print(json.dumps(course, indent=4))
        # print(json.dumps(new_course, indent=4))

        if new_course["modules"] != []:
            pass
            # print([module["module_id"] for module in new_course["modules"]])
        elif course_id in extra_module_mapping:
            pass
            # print(extra_module_mapping[course_id])
        else:
            print("no module :(")
            print(course_id, course["courseTitle"]["value"])

            print("org_id", org_id, new_course["org"]["org_id"])

    # response = requests.get("https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student/modules", headers=headers)
    # print(response.text)


except requests.exceptions.RequestException as e:
    print(f"An error occurred: {e}")
