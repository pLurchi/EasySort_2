VanillaSort is a simple plugin that allows you to sort items in a chest by using a trapped chest named `Sorter`.

You surely have already used similar sorting plugins, they all require you to do some configuration, either with item frames, signs, or commands. VanillaSort is different, it's a simple plugin that does not require any configuration.

It works by detecting the chest that already contains the item you want to sort, then moving the item to that chest.

So if you put a wooden log in a barrel, then put a wooden log in the Sorter chest, the wooden log will be sent to the barrel.

Start by creating a trapped chest and naming it `Sorter` in an anvil.

When the trapped chest closes, the items will be magically sorted to nearby chests.

The magic code it's quite natural:

- It check for nearby chest and barrels in a 16x16x5 area.
- Chest that contains that exact item are prioritized.
- Chest that contains the most amount of items are prioritized.
- Chest that contain the most diverse items are prioritized when one that contain an match cannot be found.

So basically, put your Sorter chest in the middle of your storage room, adjacent barrels and chest will be filled with the items you put in the Sorter chest.

In summary, you want to put a wooded long in a barrel, then every time you put a wooden log in the Sorter chest, it will be sent to the barrel.

So to create an automatic sorting system, put a single item in a chest, then when you put an item in the Sorter chest, it will be sent to that chest.

Note:

- Trapped chest and hoppers are not valid targets; only chest, double chest and barrel are.
- There are no configuration for this plugin, if you install it, it will work for every player on the server.
- The area is not configurable, it's hardcoded to 16x16x5.
