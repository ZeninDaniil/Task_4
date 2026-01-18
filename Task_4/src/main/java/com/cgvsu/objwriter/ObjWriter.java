package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

public class ObjWriter {

    private ObjWriter() {
    }

    public static String write(final Model model, final boolean applyModelTransform) {
        StringBuilder sb = new StringBuilder();

        Matrix4f M = applyModelTransform ? model.getModelMatrix() : null;

        for (Vector3f v : model.vertices) {
            if (M != null) {
                Vector3fImpl r = M.transformPoint(new Vector3fImpl(v.x, v.y, v.z));
                sb.append("v ").append(r.getX()).append(' ').append(r.getY()).append(' ').append(r.getZ()).append('\n');
            } else {
                sb.append("v ").append(v.x).append(' ').append(v.y).append(' ').append(v.z).append('\n');
            }
        }

        for (Vector2f vt : model.textureVertices) {
            sb.append("vt ").append(vt.x).append(' ').append(vt.y).append('\n');
        }

        for (Vector3f vn : model.normals) {
            sb.append("vn ").append(vn.x).append(' ').append(vn.y).append(' ').append(vn.z).append('\n');
        }

        for (Polygon p : model.polygons) {
            sb.append("f");
            int n = p.getVertexIndices().size();
            for (int i = 0; i < n; i++) {
                int vi = p.getVertexIndices().get(i) + 1;

                Integer vti = (p.getTextureVertexIndices() != null && p.getTextureVertexIndices().size() > i)
                        ? p.getTextureVertexIndices().get(i) + 1
                        : null;

                Integer vni = (p.getNormalIndices() != null && p.getNormalIndices().size() > i)
                        ? p.getNormalIndices().get(i) + 1
                        : null;

                sb.append(' ');
                sb.append(vi);
                if (vti != null || vni != null) {
                    sb.append('/');
                    if (vti != null) {
                        sb.append(vti);
                    }
                    if (vni != null) {
                        sb.append('/').append(vni);
                    }
                }
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
