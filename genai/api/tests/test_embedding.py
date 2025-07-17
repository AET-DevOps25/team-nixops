import pytest
from unittest.mock import MagicMock, patch
from datetime import datetime, date
import json

from openapi_server.models.study_program import StudyProgram
from openapi_server.models.module import Module
from openapi_server.models.module_courses import ModuleCourses
from openapi_server.models.course import Course
from openapi_server.models.appointment import Appointment

from genai.embedding import embed_study_program, datetime_encoder


@pytest.fixture
def test_study_program():
    """Fixture for creating a test StudyProgram object"""
    return StudyProgram(
        study_id=1,
        program_name="Test Program",
        degree_program_name="Bachelor",
        degree_type_name="B.Sc.",
        semesters={
            "WS2023": [
                Module(
                    id=101,
                    code="M101",
                    title="Test Module",
                    content="Module content",
                    outcome="Learning outcomes",
                    methods="Teaching methods",
                    exam="Exam details",
                    credits=5,
                    courses=ModuleCourses(
                        lectures=[
                            Course(
                                course_id=1001,
                                course_type="Lecture",
                                course_name="Test Course",
                                appointments=[
                                    Appointment(
                                        appointment_id=1,
                                        series_begin_date=date(2023, 10, 1),
                                        series_end_date=date(2024, 2, 1),
                                        begin_time="10:00",
                                        end_time="12:00",
                                        weekdays=["Mo"],
                                    )
                                ],
                            )
                        ]
                    ),
                )
            ]
        },
    )


def test_datetime_encoder():
    """Test the datetime_encoder class"""
    # Test with date
    test_date = date(2023, 1, 1)
    result = datetime_encoder().default(test_date)
    assert result == "2023-01-01"

    # Test with datetime
    test_datetime = datetime(2023, 1, 1, 12, 0)
    result = datetime_encoder().default(test_datetime)
    assert result == "2023-01-01 12:00:00"


def verify_description_content(desc, mod):
    """Helper function to verify all important fields are in the description"""
    assert str(mod.id) in desc
    assert mod.code in desc
    if mod.content:
        assert mod.content in desc
    if mod.outcome:
        assert mod.outcome in desc
    if mod.methods:
        assert mod.methods in desc
    if mod.exam:
        assert mod.exam in desc
    assert str(mod.credits) in desc
    assert len(desc) <= 16192  # Verify length limit


def test_description_construction_complete_module(test_study_program):
    """Test description construction with all fields populated"""
    mock_milvus = MagicMock()
    mock_milvus.query.return_value = []
    mock_embed_text = MagicMock(return_value=[0.1, 0.2, 0.3])

    embed_study_program(
        milvus_client=mock_milvus,
        embed_text=mock_embed_text,
        create_collection=MagicMock(),
        sql_session=MagicMock(),
        study_program=test_study_program,
    )

    inserted_data = mock_milvus.insert.call_args[1]["data"][0]
    mod = test_study_program.semesters["WS2023"][0]
    verify_description_content(inserted_data["description"], mod)


def test_description_construction_minimal_module():
    """Test description construction with minimal module data"""
    study_program = StudyProgram(
        study_id=1,
        program_name="Minimal Program",
        semesters={
            "WS2023": [
                Module(
                    id=201,
                    code="M201",
                    title="Minimal Module",
                    credits=3,
                    courses=ModuleCourses(),
                )
            ]
        },
    )

    mock_milvus = MagicMock()
    mock_milvus.query.return_value = []
    mock_embed_text = MagicMock(return_value=[0.1, 0.2, 0.3])

    embed_study_program(
        milvus_client=mock_milvus,
        embed_text=mock_embed_text,
        create_collection=MagicMock(),
        sql_session=MagicMock(),
        study_program=study_program,
    )

    inserted_data = mock_milvus.insert.call_args[1]["data"][0]
    mod = study_program.semesters["WS2023"][0]
    verify_description_content(inserted_data["description"], mod)
    assert (
        "Description" in inserted_data["description"]
    )  # Field label should exist even without content


def test_description_construction_with_empty_fields():
    """Test description construction with empty optional fields"""
    study_program = StudyProgram(
        study_id=1,
        program_name="Empty Fields Program",
        semesters={
            "WS2023": [
                Module(
                    id=301,
                    code="M301",
                    title="Empty Fields Module",
                    content="",
                    outcome=None,
                    methods="",
                    exam=None,
                    credits=4,
                    courses=ModuleCourses(),
                )
            ]
        },
    )

    mock_milvus = MagicMock()
    mock_milvus.query.return_value = []
    mock_embed_text = MagicMock(return_value=[0.1, 0.2, 0.3])

    embed_study_program(
        milvus_client=mock_milvus,
        embed_text=mock_embed_text,
        create_collection=MagicMock(),
        sql_session=MagicMock(),
        study_program=study_program,
    )

    inserted_data = mock_milvus.insert.call_args[1]["data"][0]
    mod = study_program.semesters["WS2023"][0]
    verify_description_content(inserted_data["description"], mod)


def test_description_construction_with_long_content():
    """Test description gets properly truncated"""
    long_content = "A" * 20000
    study_program = StudyProgram(
        study_id=1,
        program_name="Long Content Program",
        semesters={
            "WS2023": [
                Module(
                    id=401,
                    code="M401",
                    title="Long Content Module",
                    content=long_content,
                    credits=2,
                    courses=ModuleCourses(),
                )
            ]
        },
    )

    mock_milvus = MagicMock()
    mock_milvus.query.return_value = []
    mock_embed_text = MagicMock(return_value=[0.1, 0.2, 0.3])

    embed_study_program(
        milvus_client=mock_milvus,
        embed_text=mock_embed_text,
        create_collection=MagicMock(),
        sql_session=MagicMock(),
        study_program=study_program,
    )

    inserted_data = mock_milvus.insert.call_args[1]["data"][0]
    assert len(inserted_data["description"]) == 16192
    assert inserted_data["description"].endswith("A")  # Should be properly truncated


def test_description_includes_courses_json(test_study_program):
    """Test that courses JSON is included in the data"""
    mock_milvus = MagicMock()
    mock_milvus.query.return_value = []
    mock_embed_text = MagicMock(return_value=[0.1, 0.2, 0.3])

    embed_study_program(
        milvus_client=mock_milvus,
        embed_text=mock_embed_text,
        create_collection=MagicMock(),
        sql_session=MagicMock(),
        study_program=test_study_program,
    )

    inserted_data = mock_milvus.insert.call_args[1]["data"][0]
    courses_json = inserted_data["courses"]
    assert courses_json is not None
    courses_data = json.loads(courses_json)
    assert "lectures" in courses_data
    assert len(courses_data["lectures"]) == 1
    assert courses_data["lectures"][0]["courseName"] == "Test Course"
