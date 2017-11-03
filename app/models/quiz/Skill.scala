package models.quiz

import models.SkillId

case class Skill(id: SkillId, name: String, short_name: String, intercept: Double, correct: Double, incorrect: Double) {
  def β = intercept
  def γ = correct
  def ρ = incorrect
}