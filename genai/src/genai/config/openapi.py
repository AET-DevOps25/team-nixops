import yaml


def custom_openapi():
    with open("openapi.yml", "r") as genai_file:
        with open("../scraper/openapi.yaml", "r") as scraper_file:
            if genai_file is not None and scraper_file is not None:
                genai_openapi = yaml.safe_load(genai_file)
                scraper_openapi = yaml.safe_load(scraper_file)
                genai_openapi["components"]["schemas"]["Semester"] = scraper_openapi[
                    "components"
                ]["schemas"]["Semester"]
                genai_openapi["components"]["schemas"]["StudyProgram"] = (
                    scraper_openapi["components"]["schemas"]["StudyProgram"]
                )
                genai_openapi["components"]["schemas"]["Module"] = scraper_openapi[
                    "components"
                ]["schemas"]["Module"]
                genai_openapi["components"]["schemas"]["ModuleCourses"] = (
                    scraper_openapi["components"]["schemas"]["ModuleCourses"]
                )
                genai_openapi["components"]["schemas"]["Course"] = scraper_openapi[
                    "components"
                ]["schemas"]["Course"]
                genai_openapi["components"]["schemas"]["Appointment"] = scraper_openapi[
                    "components"
                ]["schemas"]["Appointment"]
                return genai_openapi
            else:
                return {}
