package michi.bot.commands

/**
 * Classes that extend [MichiCommand] with this annotation won't be added to the CommandList and thus the command
 * won't be registered.
 * This annotation should only be used in classes that extend MichiCommand, since it won't do anything when used in
 * other non-command classes.
 * @author Slz
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class CommandNotImplemented