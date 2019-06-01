
import pytesseract as pt
from PIL import Image
import cv2
import numpy as np
from matplotlib import pyplot as plt
from roi_detector import crop_receipt

try:
    f = open('tesseract_path.txt', 'r')
    tesseract_dir = f.readline()
except FileNotFoundError:
    tesseract_dir = input("Enter tesseract.exe directory")
    f = open('tesseract_path.txt', 'w')
    f.write(tesseract_dir)
    f.close()

pt.pytesseract.tesseract_cmd = r"" + tesseract_dir + "/tesseract.exe"


path = 'test_images/002.JPG'

def ocr(path):
	image = cv2.imread(path)
	cropped = crop_receipt(image)
	return pt.image_to_string(cropped)




#print(ocr(path))
