package internal

// This file contains internal extensions for Gradle API.

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Retrieves a boolean property from the project, interpreting any value other than `"false"` as
 * `true`.
 *
 * @param name The name of the property to retrieve.
 * @param defaultValue The default boolean value to return if the property is not set. Defaults to
 *   `false`.
 * @return The boolean value of the property, or [defaultValue] if the property is not set.
 * @receiver Gradle [Project] from which the property is read.
 */
fun Project.getBooleanProperty(name: String, defaultValue: Boolean = false): Boolean {
  return if (hasProperty(name)) findProperty(name)?.toString() != "false" else defaultValue
}

/**
 * Determines whether this task is a test task. For simplicity, a task is considered a test task if
 * its name contains "test" (case-insensitive).
 *
 * @return `true` if the task name matches a test task pattern, `false` otherwise.
 */
fun Task.isTestTask(): Boolean = name.matches(Regex(".*[tT]est.*"))
