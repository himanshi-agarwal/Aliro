from flask import Flask, request, jsonify
import firebase_admin
from firebase_admin import credentials, storage
from google.cloud import vision
import io

app = Flask(__name__)

# Firebase Admin SDK initialization
cred = credentials.Certificate("aliro-e0742-firebase-adminsdk-jm9vr-a3f9187e19.json")
firebase_admin.initialize_app(cred, {'storageBucket': 'aliro-436816.appspot.com'})

# Google Vision Client
vision_client = vision.ImageAnnotatorClient()


@app.route('/detect-text', methods=['POST'])
def detect_text():
    try:
        # Get user_id from the request JSON
        data = request.get_json()
        user_id = data.get("user_id")

        if not user_id:
            return jsonify({"error": "user_id is required"}), 400

        # Retrieve image from Firebase Storage
        bucket = storage.bucket()
        blob = bucket.blob(f"images/{user_id}.jpg")

        # Check if the blob exists and download the image
        image_data = blob.download_as_bytes()

        # Perform text detection using Google Vision API
        image = vision.Image(content=image_data)
        response = vision_client.text_detection(image=image)

        # Error handling for Google Vision API
        if response.error.message:
            return jsonify({"error": response.error.message}), 500

        # Extract the detected text
        texts = response.text_annotations
        if texts:
            detected_text = texts[0].description
        else:
            detected_text = "No text detected"

        return jsonify({"detected_text": detected_text})

    except firebase_admin.exceptions.NotFound:
        return jsonify({"error": "Image not found for the provided user_id"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
