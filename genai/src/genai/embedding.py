from datetime import datetime, date
import json

from sqlalchemy.orm import Session

from openapi_server.models.study_program import StudyProgram

from genai.db.relational_db import (
    StudyProgram as SqlStudyProgram,
    Semester as SqlSemester,
)


# fixes "datetime.datetime not JSON serializable"
class datetime_encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (datetime, date)):
            return str(obj)
        return json.JSONEncoder.default(self, obj)


def embed_study_program(
    milvus_client,
    embed_text,
    create_collection,
    sql_session: Session,
    study_program: StudyProgram,
) -> None:
    sem = list(map(lambda x: SqlSemester(name=x), study_program.semesters.keys()))

    print(
        "Embedding:",
        study_program.study_id,
        study_program.program_name,
        list(study_program.semesters.keys()),
    )

    for s in sem:
        collection_name = f"_{study_program.study_id}_{s.name}"
        create_collection(collection_name)
        milvus_client.load_collection(collection_name)

        try:
            existing_records = milvus_client.query(
                collection_name=collection_name,
                filter="id != 0",
                output_fields=["id"],
            )
            existing_ids = {record["id"] for record in existing_records}
        except Exception as e:
            print(
                f"Warning: failed to query existing records in {collection_name}: {e}"
            )
            existing_ids = set()

        modules = study_program.semesters[s.name]
        for n, mod in enumerate(modules):
            if mod.id in existing_ids:
                print(f"Skipping duplicate module {mod.id} in {collection_name}")
                continue

            desc = (
                "Id:"
                + str(mod.id)
                + "\n\nCode:"
                + mod.code
                + "\n\nDescription"
                + (mod.content or "")
                + "\n\n"
                + (mod.outcome or "")
                + "\n\n"
                + (mod.methods or "")
                + "\n\n"
                + (mod.exam or "")
                + "\n\nCredits: "
                + str(mod.credits)
            )[:16192]

            courses = json.dumps(mod.courses.to_dict(), cls=datetime_encoder)
            data = {
                "id": mod.id,
                "code": mod.code,
                "name": mod.title[:256],
                "description": desc,
                "description_vec": embed_text(desc),
                "courses": courses,
            }

            res = milvus_client.insert(collection_name=collection_name, data=[data])
            milvus_client.flush(collection_name=collection_name)
            print(f"Embedded module {n}/{len(modules)} ({res})")

    print("Finished embedding")

    sp = SqlStudyProgram(
        id=study_program.study_id,
        name=study_program.program_name,
        degree_program_name=study_program.degree_program_name,
        degree_type_name=study_program.degree_type_name,
        semesters=sem,
    )
    sql_session.merge(sp)
    sql_session.commit()
