<script setup>
</script>

<template>
    <div class="carousel-card">
        <el-carousel height="340px" autoplay :interval="5200" indicator-position="outside">
            <el-carousel-item>
                <div class="slide slide-brand">
                    <img src="@/assets/logo.png" alt="logo" />
                    <p>BEYEAH LUXURY SELECT</p>
                </div>
            </el-carousel-item>
            <el-carousel-item>
                <div class="slide">
                    <img src="@/assets/iPhone.jpeg" alt="iPhone" />
                </div>
            </el-carousel-item>
            <el-carousel-item>
                <div class="slide">
                    <img src="@/assets/iPad.jpg" alt="iPad" />
                </div>
            </el-carousel-item>
        </el-carousel>
    </div>
</template>

<style scoped lang="scss">
.carousel-card {
    border-radius: 24px;
    padding: 12px;
    border: 1px solid rgb(255 255 255 / 16%);
    background:
        linear-gradient(130deg, rgb(13 14 18 / 92%), rgb(66 16 24 / 65%));
    box-shadow:
        0 22px 38px rgb(0 0 0 / 30%),
        inset 0 0 0 1px rgb(255 255 255 / 6%);
}

.slide {
    width: 100%;
    height: 340px;
    border-radius: 16px;
    overflow: hidden;
    position: relative;
    background: #0f1014;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }
}

.slide-brand {
    display: grid;
    place-items: center;
    gap: 14px;
    background:
        radial-gradient(circle at 72% 18%, rgb(247 37 61 / 22%), transparent 45%),
        linear-gradient(145deg, #14151b, #220f15);

    img {
        width: 180px;
        height: 180px;
        object-fit: contain;
    }

    p {
        letter-spacing: 0.24em;
        font-size: 14px;
        color: rgb(255 255 255 / 82%);
    }
}

@media (max-width: 768px) {
    .slide,
    :deep(.el-carousel__container) {
        height: 240px !important;
    }

    .slide-brand img {
        width: 120px;
        height: 120px;
    }
}
</style>
