# Spec Delta

## MODIFIED Requirements

### Requirement: Administrative Plugin Reload Command
The system SHALL provide a `/letitroll reload` command accessible only to administrators that reloads plugin configuration and reconciles active slot machines and their displays against storage without destroying valid existing displays.

#### Scenario: Admin executes reload command
- **WHEN** an administrator executes `/letitroll reload`
- **THEN** the system reloads plugin configuration, reconciles each active machine's displays so that valid displays are preserved and duplicates or orphans are removed, and reports success to the administrator

#### Scenario: Reload preserves displayed icons
- **WHEN** an administrator executes `/letitroll reload` while a machine's displays show a spin result
- **THEN** the displays continue to show that result rather than being reset to a default icon

#### Scenario: Non-admin attempts reload
- **WHEN** a player without administrator permissions executes `/letitroll reload`
- **THEN** the system denies execution and displays a permission error message
