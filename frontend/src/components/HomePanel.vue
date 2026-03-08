<script setup>
defineProps({
    title: {
        type: String,
        default: "",
    },
    subTitle: {
        type: String,
        default: "",
    },
});
</script>

<template>
    <section class="home-panel">
        <div class="head">
            <h3>{{ title }}</h3>
            <p>{{ subTitle }}</p>
        </div>
        <slot />
    </section>
</template>

<style scoped lang="scss">
.home-panel {
    border-radius: 22px;
    padding: 24px 22px 22px;
    border: 1px solid rgb(255 255 255 / 12%);
    background:
        linear-gradient(135deg, rgb(16 17 22 / 92%), rgb(24 18 24 / 90%));
    box-shadow: 0 18px 30px rgb(0 0 0 / 22%);
}

.head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 14px;
    padding: 4px 2px 18px;
    margin-bottom: 8px;
    border-bottom: 1px solid rgb(255 255 255 / 10%);

    h3 {
        margin: 0;
        color: #fff;
        font-size: clamp(20px, 2.2vw, 30px);
        line-height: 1.2;
        letter-spacing: 0.06em;
    }

    p {
        margin: 0;
        color: var(--lux-text-muted);
        font-size: 13px;
        letter-spacing: 0.05em;
    }
}

@media (max-width: 768px) {
    .head {
        flex-direction: column;
        align-items: flex-start;
        gap: 6px;
    }
}
</style>
