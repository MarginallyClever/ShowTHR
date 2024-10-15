# ShowTHR

Read a THR file and simulate the motion of a ball rolling over a table covered in sand.

## Usage

Get the [Release](https://github.com/MarginallyClever/ShowTHR/releases) version or build it yourself from source code.
Run it from the command line.

```java -jar ShowTHR.jar <source.thr> <output> [-w <width>] [-h <height>] [-d <depth>] [-b <radius>]```

where `<requires a value>` and `[optional parts]`

- `<source.thr>`: The path to the THR file.
- `<output>`: The path to the output file.  ImageIO supported formats are accepted, including pio and webp.
- `-w <width>`: The width of the output image.  Default is 300.
- `-h <height>`: The height of the output image.  Default is 300.
- `-b <radius>`: The radius of the ball.  Default is 5.
- `-d <depth>`: The starting depth of the sand.  Default is 2.

## Example

```java -jar ShowTHR.jar "src/test/resources/Vaporeon with Waves.thr" sand_simulation.png -w 1000 -h 1000```

should produce the following:

![Example](sand_simulation.png)

## Notes

The intensity of the output image is dictated by highest peak in the sand simulation.  The output image is normalized to the range [0, 255].
If one point of sand is very tall, the rest of the image will be very dark.

## License

Apache 2.0 License
