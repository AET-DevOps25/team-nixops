# coding: utf-8

from fastapi.testclient import TestClient


from typing import Any, List  # noqa: F401
from openapi_server.models.error import Error  # noqa: F401
from openapi_server.models.study_program import StudyProgram  # noqa: F401
from openapi_server.models.study_program_selector_item import (
    StudyProgramSelectorItem,
)  # noqa: F401


def test_create_study_program(client: TestClient):
    """Test case for create_study_program

    Create a new study program and embed the modules.
    """
    study_program = {
        "degree_program_name": "Bachelor of Science in Computer Science",
        "program_name": "Computer Science",
        "degree_type_name": "Bachelor",
        "study_id": 171017263,
        "semesters": {
            "key": [
                {
                    "methods_en": "Lecture, Exercises (EN)",
                    "courses": {
                        "other": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                        "tutorials": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                        "lectures": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                    },
                    "code": "CS123",
                    "methods": "Lecture, Exercises",
                    "content_en": "Covers basics such as loops, variables, conditionals... (EN)",
                    "title": "Introduction to Programming",
                    "exam_en": "Written exam (EN)",
                    "content": "Covers basics such as loops, variables, conditionals...",
                    "exam": "Written exam",
                    "credits": 5.0,
                    "title_en": "Introduction to Programming (EN)",
                    "id": "CS123",
                    "outcome": "Students can write basic code.",
                    "outcome_en": "Students can write basic code. (EN)",
                },
                {
                    "methods_en": "Lecture, Exercises (EN)",
                    "courses": {
                        "other": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                        "tutorials": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                        "lectures": [
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                            {
                                "course_type": "exercise",
                                "course_name": "Advanced Algorithms",
                                "course_name_list": "Algorithms, Data Structures",
                                "appointments": [
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                    {
                                        "series_end_date": "2024-07-01",
                                        "weekdays": ["mo", "di"],
                                        "series_begin_date": "2024-03-01",
                                        "appointment_id": 1,
                                        "begin_time": "08:00",
                                        "end_time": "10:00",
                                    },
                                ],
                                "course_name_list_en": "Algorithms, Data Structures (EN)",
                                "course_name_en": "Advanced Algorithms (EN)",
                                "course_id": 12345,
                            },
                        ],
                    },
                    "code": "CS123",
                    "methods": "Lecture, Exercises",
                    "content_en": "Covers basics such as loops, variables, conditionals... (EN)",
                    "title": "Introduction to Programming",
                    "exam_en": "Written exam (EN)",
                    "content": "Covers basics such as loops, variables, conditionals...",
                    "exam": "Written exam",
                    "credits": 5.0,
                    "title_en": "Introduction to Programming (EN)",
                    "id": "CS123",
                    "outcome": "Students can write basic code.",
                    "outcome_en": "Students can write basic code. (EN)",
                },
            ]
        },
    }

    headers = {}
    # uncomment below to make a request
    # response = client.request(
    #    "POST",
    #    "/embed",
    #    headers=headers,
    #    json=study_program,
    # )

    # uncomment below to assert the status code of the HTTP response
    # assert response.status_code == 200


def test_fetch_study_programs(client: TestClient):
    """Test case for fetch_study_programs

    Get all scraped study programs
    """

    headers = {}
    # uncomment below to make a request
    # response = client.request(
    #    "GET",
    #    "/embed/studyPrograms",
    #    headers=headers,
    # )

    # uncomment below to assert the status code of the HTTP response
    # assert response.status_code == 200
