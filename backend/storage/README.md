# Backend Asset Storage

This directory stores runtime static assets for local development.

- `goods-img/`: product cover images served at `/goods-img/**`
- `upload/`: admin upload files served at `/upload/**`

Path strategy:

- Backend uses relative paths (`./storage/goods-img/`, `./storage/upload/`) from the backend working directory.
- Database `goods_cover_img` should store web paths such as `/goods-img/your-file.jpg`.

