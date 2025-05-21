# CoreAPI

CoreAPI is a powerful and flexible framework designed for monitoring, versioning, and maintaining functionality of all CoreFramework plugins as well as creating interactive inventory menus in non-CoreFramework Bukkit/Spigot/Paper plugins. CoreAPI provides a clean, intuitive API for building simple to complex modal interfaces with advanced features like pagination, dynamic titles, and nested navigation.

## Terminology - Modals vs GUIs

In CoreAPI, we use the term "modal" instead of the more commonly used "GUI" or "inventory menu" in the Minecraft plugin community. While these terms are essentially interchangeable in this context, we chose "modal" to better reflect the interactive, focused nature of these interfaces.

A modal represents a self-contained interface that temporarily takes focus and requires user interaction before returning to the main gameplay. This terminology aligns with modern UI/UX concepts and helps distinguish between different layers of interaction within your plugin.

Don't worry though - whether you call them: modals, GUIs, menus, or inventories, CoreAPI works the same way and seamlessly integrates with the standard Bukkit inventory protocols.

## Features

- **Simple Builder API**: Create modals with minimal, readable code
- **Paginated Modals**: Easily create paginated interfaces with automatic page navigation
- **Dynamic Titles**: Titles that update based on pagination or player interactions
- **Pagination Regions**: Define specific areas for paginated content
- **Click Type Detection**: Handle different click types (left, right, shift+left, etc.)
- **Modal Navigation**: Create linked modal systems with navigation history
- **Advanced Item Builders**: Comprehensive API for creating complex items with ease

## Getting Started

### Installation

Add CoreAPI as a dependency in your plugin.yml:
```yaml
depend: [CoreAPI]
```

Or as a soft dependency:
```yaml
softdepend: [CoreAPI]
```

### Basic Usage

Creating a simple modal:
```java
PaginatedModal modal = Modal.paginated()
    .rows(3)
    .title(Component.text("My Modal"))
    .create();

// Add items
modal.setItem(13, ItemBuilder.from(Material.DIAMOND)
    .name(Component.text("Click me!"))
    .asModalItem(event -> {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("You clicked the diamond!");
    }));

// Open for player
modal.open(player);
```

### Advanced Features

#### Dynamic Titles

```java
PaginatedModal modal = Modal.paginated()
    .rows(3)
    .dynamicTitle("My Modal")  // Updates with pagination
    .create();

// Or interaction-based dynamic titles:
modal.setDynamicTitle("My Modal", (state) -> {
    if (state.clickedItem != null) {
        return Component.text("Selected: " + state.clickedItem.getItemStack().getType().name());
    }
    return Component.text("Select an item");
});
```

#### Pagination Regions

```java
PaginationRegion region = PaginationRegion.rectangle(2, 1, 5, 8);

PaginatedModal modal = Modal.paginated()
    .rows(6)
    .paginationRegion(region)
    .create();
```

#### Modal Navigation

```java
// Create a main modal
PaginatedModal mainModal = Modal.paginated()
    .rows(3)
    .title(Component.text("Main Menu"))
    .create();

// Create a sub modal
PaginatedModal subModal = Modal.paginated()
    .rows(3)
    .title(Component.text("Sub Menu"))
    .create();

// Add navigation button
mainModal.setItem(13, NavBuilder.navigateTo(
    Material.COMPASS,
    Component.text("Sub Menu"),
    subModal
));

// Add back button
subModal.setItem(26, NavBuilder.back());
```

## Documentation

For complete documentation, see the [Wiki](https://github.com/rhythmknights/CoreAPI/wiki) or the JavaDocs.

## Examples

Check the `examples` package in the source code for comprehensive examples of different modal types and features.

## Acknowledgements

CoreAPI is based on [TriumphGUI](https://github.com/TriumphTeam/triumph-gui) by TriumphTeam. This project wouldn't be possible without their excellent work. We've extended and modified their codebase to add new features while maintaining the clean API design that made TriumphGUI great.

Special thanks to:
- TriumphTeam for the original TriumphGUI library
- The Bukkit/Spigot community for their support and feedback
- All contributors to this project

## License

CoreAPI is licensed under the MIT License - see the LICENSE file for details.
