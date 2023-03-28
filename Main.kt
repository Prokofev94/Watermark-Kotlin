package watermark

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Input the image filename:")
    val imageFileName = readln()
    val imageFile = File(imageFileName)

    if (!imageFile.exists()) {
        println("The file $imageFileName doesn't exist.")
        return
    }

    val image = ImageIO.read(imageFile)

    if (image.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        return
    }

    if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
        println("The image isn't 24 or 32-bit.")
        return
    }


    println("Input the watermark image filename:")
    val watermarkFileName = readln()
    val watermarkFile = File(watermarkFileName)

    if (!watermarkFile.exists()) {
        println("The file $watermarkFileName doesn't exist.")
        return
    }

    val watermark = ImageIO.read(watermarkFile)

    if (watermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        return
    }

    if (watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) {
        println("The watermark isn't 24 or 32-bit.")
        return
    }

    if (watermark.height > image.height || watermark.width > image.width) {
        println("The watermark's dimensions are larger.")
        return
    }


    var alphaChannel = watermark.transparency == 3
    var colorAsTransparency = false
    var specificColor = Color.WHITE
    if (alphaChannel) {
        println("Do you want to use the watermark's Alpha channel?")
        alphaChannel = readln().lowercase() == "yes"
    } else {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            try {
                val rgbList = readln().split(' ').map { it.toInt() }
                if (rgbList.size != 3) throw Exception()
                specificColor = Color(rgbList[0], rgbList[1], rgbList[2])
                colorAsTransparency = true
            } catch (e: Exception) {
                println("The transparency color input is invalid.")
                return
            }
        }
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val weight = readln().toIntOrNull()

    if (weight !is Int) {
        println("The transparency percentage isn't an integer number.")
        return
    }

    if (weight !in 0..100) {
        println("The transparency percentage is out of range.")
        return
    }

    println("Choose the position method (single, grid):")
    val positionType = readln()
    if (positionType != "single" && positionType != "grid") {
        println("The position method input is invalid.")
        return
    }

    var posX = 0
    var posY = 0
    val diffX = image.width - watermark.width
    val diffY = image.height - watermark.height
    if (positionType == "single") {
        println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
        try {
            val position = readln().split(' ').map { it.toInt() }
            if (position.size != 2) throw Exception()
            posX = position[0]
            posY = position[1]
            if (posX !in 0..diffX || posY !in 0..diffY) {
                println("The position input is out of range.")
                return
            }
        } catch (e: Exception) {
            println("The position input is invalid.")
            return
        }
    }


    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()

    if (!outputFileName.endsWith(".jpg") && !outputFileName.endsWith(".png")) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }

    val extension = outputFileName.substring(outputFileName.length - 3)
    val outputFile = File(outputFileName)

    if (positionType == "grid") {
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val i = Color(image.getRGB(x, y))
                val w = Color(watermark.getRGB(x % watermark.width, y % watermark.height), alphaChannel)
                if (alphaChannel && w.alpha == 0 || colorAsTransparency && w.rgb == specificColor.rgb) continue
                val color = Color(
                    (weight * w.red + (100 - weight) * i.red) / 100,
                    (weight * w.green + (100 - weight) * i.green) / 100,
                    (weight * w.blue + (100 - weight) * i.blue) / 100
                )
                image.setRGB(x, y, color.rgb)
            }
        }
    } else {
        for (x in 0 until watermark.width) {
            for (y in 0 until watermark.height) {
                val i = Color(image.getRGB(x + posX, y + posY))
                val w = Color(watermark.getRGB(x, y), alphaChannel)
                if (alphaChannel && w.alpha == 0 || colorAsTransparency && w.rgb == specificColor.rgb) continue
                val color = Color(
                    (weight * w.red + (100 - weight) * i.red) / 100,
                    (weight * w.green + (100 - weight) * i.green) / 100,
                    (weight * w.blue + (100 - weight) * i.blue) / 100
                )
                image.setRGB(x + posX, y + posY, color.rgb)
            }
        }
    }

    ImageIO.write(image, extension, outputFile)
    println("The watermarked image $outputFileName has been created.")
}