The rule engine is designed to take as input the user preferences of
white and blacklisted inferences. It implements the following:

a. A language (syntax + parser) specifying the format in which the system
expects the input for black/white listed inferences.
b. A GUI which the users can use directly to specify their preferences.
c. A GUI through which users would be able to configure/customize a set
of already defined privacy rule.
d. A crowd-sourced interface for privacy rules associated with apps
(future).
e. A model containing the relationship between the different inferences
(a DBN).
f. A evaluation block which determines the set of policies to be applied
on the data for the requesting apps.

Interfaces
==========
a. Interfaces with the context engine (to receive context information)
b. Interface with the privacy engine to provide the set of rules to be
applied.
