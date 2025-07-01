from typing import List
from typing import Optional
from sqlalchemy import ForeignKey
from sqlalchemy import String
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import mapped_column
from sqlalchemy.orm import relationship
from sqlalchemy import create_engine


class Base(DeclarativeBase):
    pass


class StudyProgram(Base):
    __tablename__ = "study_program"
    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(30))
    degree_program_name: Mapped[str] = mapped_column(String(30))
    degree_type_name: Mapped[str] = mapped_column(String(30))
    semesters: Mapped[List["Semester"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )

    def __repr__(self) -> str:
        return f"StudyProgram(id={self.id!r}, name={self.name!r})"


class Semester(Base):
    __tablename__ = "semester"
    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(30))

    def __repr__(self) -> str:
        return f"Semester(id={self.id!r}, name={self.name!r})"


engine = create_engine("postgresql://pguser:pgpass@localhost:5432/nixops", echo=True)


def create_db_and_tables():
    Base.metadata.create_all(engine)
