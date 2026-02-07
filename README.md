Allows infinite fluid source like minecraft.

By default, only water is configured, but all the hytale and modded fluid can be configured if they follow vanilla hytale naming convention.


### Configuration option
Example, if you want to add lava act like infinite source like minecraft gamerule for infinite lava do 
```json
{
  "Fluids": {
    "Water": {
      "Enabled": true
    },
    "Lava": {
      "Enabled": true
    }
  }
}
```
you can also disable the default infinite water by changing `Enabled` to `false`