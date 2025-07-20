from typing import List
from typing import Optional

from urllib.parse import quote_plus

from sqlalchemy import Column, ForeignKey, Table
from sqlalchemy import String
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import mapped_column
from sqlalchemy.orm import relationship
from sqlalchemy.orm import Session
from sqlalchemy.exc import NoResultFound
from sqlalchemy import create_engine

from ..config import env


class Base(DeclarativeBase):
    pass


# Association table for the many-to-many relationship
study_program_semester = Table(
    "study_program_semester",
    Base.metadata,
    Column("study_program_id", ForeignKey("study_program.id"), primary_key=True),
    Column("semester_id", ForeignKey("semester.id"), primary_key=True),
)


class StudyProgram(Base):
    __tablename__ = "study_program"
    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(128))
    degree_program_name: Mapped[str] = mapped_column(String(128))
    degree_type_name: Mapped[str] = mapped_column(String(128))

    # Define the relationship to Semester through the association table
    semesters: Mapped[List["Semester"]] = relationship(
        "Semester",
        secondary=study_program_semester,
        back_populates="study_programs",
    )

    def __repr__(self) -> str:
        return f"StudyProgram(id={self.id!r}, name={self.name!r})"


class Semester(Base):
    __tablename__ = "semester"
    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(16))

    study_programs: Mapped[List[StudyProgram]] = relationship(
        "StudyProgram", secondary=study_program_semester, back_populates="semesters"
    )

    def __repr__(self) -> str:
        return f"Semester(id={self.id!r}, name={self.name!r})"


database_uri = f"postgresql://{quote_plus(env.db_user)}:{quote_plus(env.db_pass)}@{env.db_host}/{quote_plus(env.db_name)}"

engine = create_engine(database_uri, echo=True)


def create_db_and_tables():
    Base.metadata.create_all(engine)


def get_study_program_id_by_name(name: str) -> Optional[int]:
    with Session(engine) as session:
        try:
            program = (
                session.query(StudyProgram).filter(StudyProgram.name == name).one()
            )
            return program.id
        except NoResultFound:
            return None


def get_study_program_name_by_id(program_id: int) -> Optional[str]:
    with Session(engine) as session:
        try:
            program = (
                session.query(StudyProgram).filter(StudyProgram.id == program_id).one()
            )
            return program.name
        except NoResultFound:
            return None
