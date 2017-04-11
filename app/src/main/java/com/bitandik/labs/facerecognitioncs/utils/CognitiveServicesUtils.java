package com.bitandik.labs.facerecognitioncs.utils;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Category;
import com.microsoft.projectoxford.vision.contract.Face;
import com.microsoft.projectoxford.vision.contract.Tag;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ykro.
 */

public class CognitiveServicesUtils {
  private VisionServiceClient client;
  private static CognitiveServicesUtils utils;
  private ArrayList<RectF> facesRectangles;

  public CognitiveServicesUtils() {
  }

  public void setClient(VisionServiceClient client) {
    this.client = client;
  }

  public static CognitiveServicesUtils init(String APIKey) {
    if (utils == null) {
      utils = new CognitiveServicesUtils();
      utils.setClient(new VisionServiceRestClient(APIKey));
    }
    return utils;
  }

  public String getAnnotations(Bitmap bitmap) throws VisionServiceException, IOException {
    String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories", "Tags", "Description"};
    String[] details = {};

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

    AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

    String result = processResponse(v);

    return result;
  }

  public String processResponse(AnalysisResult response) {
    facesRectangles = new ArrayList<RectF>();
    String message = "I found these things:\n\n";

    if (response != null) {
      if (response.adult != null) {
        message += String.format(Locale.US, "%.3f: Adult content score\n", response.adult.adultScore);
      }


      if (response.faces != null && !response.faces.isEmpty()) {
        message += "Faces\n";

        for (Face face : response.faces) {
          message += String.format(Locale.US, "%d: Age\n", face.age);
          message += String.format(Locale.US, "%s: Gender\n", face.gender);

          RectF faceRectangle = new RectF(face.faceRectangle.left,
                                          face.faceRectangle.top,
                                          face.faceRectangle.left + face.faceRectangle.width,
                                          face.faceRectangle.top + face.faceRectangle.height);
          facesRectangles.add(faceRectangle);
        }
        message += "\n";
      }

      if (response.description != null) {
        if (response.description.tags != null) {
          message += "Description tags\n" + response.description.tags.toString() + "\n";
        }

        List<Caption> captions = response.description.captions;
        if (captions != null) {
          message += "Captions\n";
          for (Caption caption : captions) {
            message += String.format(Locale.US, "%.3f: %s\n", caption.confidence, caption.text);
          }
          message += "\n";
        }
      }

      if (response.tags != null) {
        List<Tag> tags = response.tags;
        if (tags != null) {
          message += "Tags\n";
          for (Tag tag : tags) {
            message += String.format(Locale.US, "%.3f: %s\n", tag.confidence, tag.name);
          }
          message += "\n";
        }
      }

      if (response.categories != null) {
        List<Category> categories = response.categories;
        if (categories != null) {
          message += "Categories\n";
          for (Category category : categories) {
            message += String.format(Locale.US, "%.3f: %s\n", category.score, category.name);
          }
          message += "\n";
        }
      }
    }
    return message;
  }

  public ArrayList<RectF> getFacesRectangles() {
    return this.facesRectangles;
  }
}
