# Code Review Rules

## 1. JSON (.json)

Syntax: strict syntax required. No trailing commas.

Data Integrity (CRITICAL):

Constraint: The identifier field value MUST exist as a registered name in src/main/java/bond/thematic/mod/abilities/AbilityRegistry.java.

Action: Call out PRs containing "made up" identifiers not present in AbilityRegistry.java.

Validation:

// PASS: "heat_vision" is found in AbilityRegistry.java
{ "identifier": "heat_vision", "keybind": "key.thematic.ability_0" }


## 2. Java (.java)

Clean Code:

No Magic Numbers: Replace raw numbers with descriptive static final constants.

Null Safety: Enforce explicit null checks or Optional usage on nullable types.

Try/Catch/Finally: Ensure a try/catch/finally is in place with potentially null types or game logic that's inconsistent. Ensure there is a push and pop before the try and in the finally for all MatrixStack operations.

Comments:

Prohibited: Redundant comments on obvious logic (e.g., getters/setters).

Allowed: Explaining why complex logic exists.

Architecture: Enforce modularity and encapsulation.

Always: Use cooldown(), duration(), amplifier() in place of random numbers.

Always: Make sure classes are grouped in their right place.

Always: Call out big changes that effect more than just their current scope.

Always: Call out too many comments that are likely generated from LLMs.

Always: Call out items that are in /api/ instead of /mod/ when they're not API code.

Always: Suggest ways to limit the overall performance.

## 3. Suggesting changes

Always: Suggest a change commit when possible.

Important: All pull requests must have an update to CHANGELOG.md that reflects the changes added. Suggest edits to this based on review.