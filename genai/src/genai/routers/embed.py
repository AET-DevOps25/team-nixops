from fastapi import APIRouter
from decouple import config
from ollama import Client

from pymilvus import IndexType, MilvusClient, DataType

# NOTE: https://milvus.io/docs/use-async-milvus-client-with-asyncio.md#Create-index

router = APIRouter()

llm_api_url = config("LLM_API_URL", default="https://gpu.aet.cit.tum.de/ollama")
llm_api_key = config("LLM_API_KEY")
# mxbai-embed-large performs really well, but not available
# deepseek is the best of the available ones
llm_embedding_model = config("LLM_EMBEDDING_MODEL", default="deepseek-r1:70b") 

ollama_client = Client(
    host=llm_api_url, headers={"Authorization": f"Bearer {llm_api_key}"}
)

milvus_client = MilvusClient(uri="http://localhost:19530", token="root:Milvus")


def emb_text(text):
    response = ollama_client.embeddings(
        model=llm_embedding_model,
        prompt=text,
    )
    return response["embedding"]


def embedding_len():
    return len(emb_text("foo"))


schema = MilvusClient.create_schema(
    auto_id=False,
    enable_dynamic_field=True,
)

collection_name = "informatics"

# each row is a module
schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="name", datatype=DataType.VARCHAR, max_length=128)
schema.add_field(field_name="description", datatype=DataType.VARCHAR, max_length=2048)
schema.add_field(field_name="description_vec", datatype=DataType.FLOAT_VECTOR, dim=embedding_len())
schema.add_field(field_name="timeslots", datatype=DataType.JSON)

if milvus_client.has_collection(collection_name):
    milvus_client.drop_collection(collection_name)
milvus_client.create_collection(
    collection_name=collection_name,
    schema=schema,
)

index_params = milvus_client.prepare_index_params()
index_params.add_index(field_name="description_vec", index_type="AUTOINDEX", metric_type="COSINE")
milvus_client.create_index(collection_name, index_params)

milvus_client.load_collection(collection_name=collection_name)
res = milvus_client.get_load_state(collection_name=collection_name)
print(res)

cv_name = "Computer Vision II: Multiple View Geometry (3D Computer Vision) (IN2228)"
cv_desc = """Die Vorlesung führt in die Grundlagen der Epipolargeometrie ein.
Ziel ist die Rekonstruktion der drei-dimensionalen Welt und der
Kamerabewegung aus mehreren Bildern.  Dazu werden Punktkorrespondenzen
zwischen zwei oder mehr Bildern bestimmt und Bedingungen für die
Kamerabewegung und die Tiefe der beobachteten Punkte hergeleitet.
Besonderer Schwerpunkt ist die mathematische Beschreibung von
Starrkörperbewegungen zur Darstellung der Kamerabewegung und von
perspektivischer Projektion in der Abbildung der drei-dimensionalen Welt.
Zur Schätzung von Kamerabewegung und 3D Geometrie werden sowohl spektrale
Methoden als auch Methoden nichtlinearer Optimierung vorgestellt."""
itsec_name = "IT Sicherheit 2 (CIT3330002)"
itsec_desc = """In dem Modul werden vertiefende sowie spezielle Themen der IT-Sicherheit behandelt. Aktuelle Konzepte und Lösungen im Bereich der Digitalen Identität, wie Smartcards, Physically Unclonable Functions (PUF), SSI und Token-basierte Authentisierung  in verteilten Systemen werden vertiefend behandelt.  Im Bereich der Anwendungssicherheit werden ausgewählte Fragestellungen, wie die Sicherheit von Instant Messenger-Diensten diskutiert. Im Bereich der Systemsicherheit widmet sich das Modul fortgeschrittenen Konzepten wie dem Trusted Computing, den Trusted Execution Environments und dem Confidential Computing und den dafür vorhandenen Hardware-seitigen Sicherheitskonzepten.  Das Modul behandelt den aktuellen und in Entwicklung befindlichen Stand der Sicherheit drahtloser und mobiler Kommunikationsarchitekturen (u.a. 5G) und mit Konzepten zur ad-hoc Sicherheit bei vernetzten IoT Geräten (z.B. BluetoothLE). Das Modul wird zudem einen Einblick in die methodische Entwicklung und Bewertung von sicheren Systemen (Security Engineering) geben."""
devops_name = "DevOps: Engineering for Deployment and Operations (CIT423001)"
devops_desc = """DevOps is an integrative approach to software engineering, blending development (Dev) and operations (Ops) to optimize the software development lifecycle, enhance collaboration, and streamline workflows. It emphasizes the use of automated processes, continuous integration, and deployment strategies to improve the efficiency and quality of software systems. It has become a widely adopted practice in the software industry, since it leads to shorter release cycles of software while achieving high quality. Key topics in DevOps include, among others:
- Introduction and Overview of DevOps
- DevOps Culture and Organization
- Software Engineering in DevOps
- Virtualization, Containers, and Cloud Integration
- Deployment Strategies and Continuous Delivery
- Monitoring, Feedback, and User-Centric Approaches
- Security, Risk Management, and Compliance
- Microservices and Scalable Architectures
- Post-Production Management
- Advanced Topics and Emerging Trends"""

timeslots = {
        "ue": [{"name": "Zentralübung", "start": "2025-06-29T11:19:04Z", "end": "2025-06-29T11:19:04Z"}],
        "vo": []
        }

data = [
        {"id": 0, "name": cv_name, "description": cv_desc, "description_vec": emb_text(cv_desc), "timeslots": timeslots},
        {"id": 0, "name": itsec_name, "description": itsec_desc, "description_vec": emb_text(itsec_desc), "timeslots": timeslots},
        {"id": 0, "name": devops_name, "description": devops_desc, "description_vec": emb_text(devops_desc), "timeslots": timeslots},
]
res = milvus_client.insert(collection_name=collection_name, data=data)
milvus_client.flush(collection_name=collection_name)
print(res)

search_res = milvus_client.search(
    collection_name=collection_name,
    data=[
        emb_text("i love planning and managing")
    ],
    anns_field="description_vec", # only one anns field can exist
    limit=3,
    output_fields=["name", "timeslots"]
)
print(search_res)
