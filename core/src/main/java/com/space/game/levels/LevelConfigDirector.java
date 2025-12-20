package com.space.game.levels;

import java.util.ArrayList;
import com.space.game.entities.Spaceship;
import java.util.List;
import java.util.Random;

import com.space.game.SpaceGame;
import com.space.game.config.LevelConfig;
import com.space.game.config.LevelConfigBuilder;
import com.space.game.config.LevelTheme;

/**
 * Director que conhece as receitas para construir diferentes tipos de níveis
 * Organiza a construção step-by-step seguindo o padrão Builder + Director
 */
public class LevelConfigDirector {

    private final Random random;
    private float factorSpeedInitial;
    private float speed;
    private boolean initialized = false;

    public LevelConfigDirector() {
        this.random = new Random();
        // Removida a inicialização aqui para evitar NullPointerException
        // A inicialização será feita lazy quando necessário
    }

    /**
     * Inicializa os valores que dependem do SpaceGame estar pronto
     */
    private void ensureInitialized() {
        if (!initialized) {
            this.speed = SpaceGame.getGame().getWorldWidth();
            this.factorSpeedInitial = 70f;
            this.initialized = true;
        }
    }

    /**
     * Constrói a configuração do primeiro nível
     */
    public LevelConfig buildFirstLevel(LevelConfigBuilder builder) {
        ensureInitialized(); // Garante que está inicializado
        builder.reset();

        int enemyCount = 7;
        List<Integer> movementPatterns = new ArrayList<>();
        // Wave 1: Only linear aliens (Pattern 0)
        for (int i = 0; i < enemyCount; i++)
            movementPatterns.add(0);

        return builder
                .setBasicInfo(1)
                .setEnemyConfiguration(enemyCount, 1.0f) // Speed Multiplier 1.0
                .setMovementPatterns(movementPatterns)
                .setPlayerResources(49)
                .setPlayerStats(0, 1, 0)
                .setDarkLevel(false)
                .setTheme(LevelTheme.NEBULA_BLUE)
                .setSwarmWarning(false)
                .build();
    }

    /**
     * Constrói a configuração para níveis progressivos baseado no nível anterior
     */
    public LevelConfig buildProgressiveLevel(LevelConfigBuilder builder, int levelNumber, LevelConfig previousConfig) {
        ensureInitialized(); // Garante que está inicializado
        builder.reset();

        // Calcula novos valores baseados no nível anterior
        int newEnemyCount = calculateEnemyCount(previousConfig);
        float newEnemySpeed = calculateEnemySpeed(previousConfig);
        List<Integer> movementPatterns = generateMovementPatterns(newEnemyCount, levelNumber);

        // Obtém estatísticas atuais do jogador
        PlayerStats stats = getCurrentPlayerStats();

        return builder
                .setBasicInfo(levelNumber)
                .setEnemyConfiguration(newEnemyCount, newEnemySpeed)
                .setMovementPatterns(movementPatterns)
                .setPlayerResources(stats.ammunitions + 7)
                .setPlayerStats(stats.kills, stats.streak, stats.consecutiveKills)
                .setDarkLevel((levelNumber == 4) || (levelNumber > 10 && levelNumber % 3 == 0 && levelNumber % 2 != 0))
                .setTheme(determineTheme(levelNumber)) // New Theme Logic
                .setSwarmWarning(false)
                .build();
    }

    /**
     * Constrói um nível de desafio especial (mais difícil)
     */
    public LevelConfig buildChallengeLevel(LevelConfigBuilder builder, int levelNumber, LevelConfig baseConfig) {
        ensureInitialized(); // Garante que está inicializado
        builder.reset();

        int challengeEnemyCount = baseConfig.getEnemyCount() + random.nextInt(5) + 5; // Mais inimigos
        // Use config for challenge speed multiplier
        float challengeSpeed = baseConfig.getEnemySpeed()
                * com.space.game.config.GameConfig.CHALLENGE_LEVEL_SPEED_MULTIPLIER;
        List<Integer> challengePatterns = generateChallengeMovementPatterns(challengeEnemyCount, levelNumber);

        PlayerStats stats = getCurrentPlayerStats();

        return builder
                .setBasicInfo(levelNumber)
                .setEnemyConfiguration(challengeEnemyCount, challengeSpeed)
                .setMovementPatterns(challengePatterns)
                .setPlayerResources(stats.ammunitions + 10) // Mais munição para o desafio
                .setPlayerStats(stats.kills, stats.streak, stats.consecutiveKills)
                .setDarkLevel((levelNumber == 4) || (levelNumber > 10 && levelNumber % 3 == 0 && levelNumber % 2 != 0))
                .setTheme(LevelTheme.NEBULA_BLUE) // Challenge Theme
                .setSwarmWarning(levelNumber == 7) // Warning for Wave 7
                .build();
    }

    /**
     * Constrói um nível boss (configuração especial)
     */
    public LevelConfig buildBossLevel(LevelConfigBuilder builder, int levelNumber) {
        ensureInitialized(); // Garante que está inicializado
        builder.reset();

        int bossEnemyCount = 15 + random.nextInt(5); // Muitos inimigos
        // Use config for boss speed multiplier
        float bossSpeed = com.space.game.config.GameConfig.BOSS_LEVEL_SPEED_MULTIPLIER;
        List<Integer> bossPatterns = generateBossMovementPatterns(bossEnemyCount);

        PlayerStats stats = getCurrentPlayerStats();

        return builder
                .setBasicInfo(levelNumber)
                .setEnemyConfiguration(bossEnemyCount, bossSpeed)
                .setMovementPatterns(bossPatterns)
                .setPlayerResources(stats.ammunitions + 15)
                .setPlayerStats(stats.kills, stats.streak, stats.consecutiveKills)
                .setDarkLevel(levelNumber % 9 == 0)
                .setTheme(LevelTheme.NEBULA_BLUE) // Boss Theme
                .build();
    }

    // Métodos auxiliares para cálculos específicos

    private int calculateEnemyCount(LevelConfig previousConfig) {
        int level = previousConfig.getLevelNumber() + 1;
        int baseCount = com.space.game.config.GameConfig.BASE_ENEMY_COUNT +
                ((level - 1) * com.space.game.config.GameConfig.ENEMY_COUNT_GROWTH);

        // Random variation (+0 to +2)
        baseCount += random.nextInt(3);

        // Dark Level Nerf
        boolean isDark = (level >= 9 && level % 3 == 0 && level % 2 != 0);
        if (isDark) {
            baseCount = (int) (baseCount * com.space.game.config.GameConfig.DARK_LEVEL_COUNT_MULTIPLIER);
        }

        return baseCount;
    }

    private float calculateEnemySpeed(LevelConfig previousConfig) {
        int level = previousConfig.getLevelNumber() + 1;

        // Calculate Multiplier using config
        float speedMultiplier = 1.0f + ((level - 1) * com.space.game.config.GameConfig.ALIEN_SPEED_GROWTH_RATE);

        // Dark Level Nerf
        boolean isDark = (level >= 9 && level % 3 == 0 && level % 2 != 0);
        if (isDark) {
            speedMultiplier *= com.space.game.config.GameConfig.DARK_LEVEL_SPEED_MULTIPLIER;
        }

        return speedMultiplier;
    }

    private PlayerStats getCurrentPlayerStats() {
        Spaceship spaceship = SpaceGame.getGame().getMapManager().getSpaceship();
        return new PlayerStats(
                (int) spaceship.getEnergy(),
                spaceship.getKillCount(),
                spaceship.getStreakCount(),
                spaceship.getConsecutiveKills());
    }

    // Geração de padrões de movimento

    private List<Integer> generateMovementPatterns(int enemyCount, int levelNumber) {
        List<Integer> patterns = new ArrayList<>();
        List<Integer> weightedPatterns = createWeightedPatterns(enemyCount, levelNumber);

        for (int i = 0; i < enemyCount; i++) {
            patterns.add(weightedPatterns.get(random.nextInt(weightedPatterns.size())));
        }
        return patterns;
    }

    private List<Integer> generateChallengeMovementPatterns(int enemyCount, int levelNumber) {
        List<Integer> patterns = new ArrayList<>();
        // Para níveis de desafio, usar mais padrões complexos
        // Para níveis de desafio, usar mais padrões complexos
        for (int i = 0; i < enemyCount; i++) {
            if (levelNumber > com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL && random.nextFloat() < 0.05f) { // 5%
                                                                                                                  // Chance
                patterns.add(3); // Mix in Baby Boomers (Rare)
            } else {
                patterns.add(random.nextInt(2) + 1); // Padrões 1 e 2
            }
        }
        return patterns;
    }

    private List<Integer> generateBossMovementPatterns(int enemyCount) {
        List<Integer> patterns = new ArrayList<>();
        // Para níveis boss, usar principalmente o padrão mais difícil
        for (int i = 0; i < enemyCount; i++) {
            if (random.nextFloat() < 0.7f) {
                patterns.add(2); // 70% padrão mais difícil
            } else {
                patterns.add(1); // 30% padrão médio
            }
        }
        return patterns;
    }

    private List<Integer> createWeightedPatterns(int enemyCount, int levelNumber) {
        List<Integer> weightedPatterns = new ArrayList<>();

        if (levelNumber == 2) {
            // Wave 2: Linear (0) + Wave (1)
            int half = enemyCount / 2;
            for (int i = 0; i < half; i++)
                weightedPatterns.add(0);
            for (int i = half; i < enemyCount; i++)
                weightedPatterns.add(1);
            return weightedPatterns;
        }

        if (levelNumber == 3) {
            // Wave 3: Linear + Wave + Spiral (0, 1, 2)
            int third = enemyCount / 3;
            for (int i = 0; i < third; i++)
                weightedPatterns.add(0);
            for (int i = third; i < third * 2; i++)
                weightedPatterns.add(1);
            for (int i = third * 2; i < enemyCount; i++)
                weightedPatterns.add(2);
            return weightedPatterns;
        }

        boolean spawnBoomers = levelNumber > com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL;

        int weightFor0, weightFor1, weightFor2, weightFor3;

        if (spawnBoomers) {
            // Include Baby Boomer (Pattern 3) - Rare (Configurable Chance)
            int babyBoomerCount = 0;

            // First pass: Calculate standard weights
            weightFor0 = (int) (enemyCount * 0.45f);
            weightFor1 = (int) (enemyCount * 0.35f);
            weightFor2 = enemyCount - weightFor0 - weightFor1;

            // Attempt to inject Baby Boomers based on chance
            // Max per wave config
            int maxBabies = com.space.game.config.GameConfig.MAX_BABY_BOOMERS_PER_WAVE;

            for (int k = 0; k < maxBabies; k++) {
                if (random.nextFloat() < com.space.game.config.GameConfig.BABY_BOOMER_CHANCE_AFTER_LEVEL_10) {
                    babyBoomerCount++;
                }
            }

            // Adjust weights to fit babies
            if (babyBoomerCount > 0) {
                if (weightFor0 > babyBoomerCount)
                    weightFor0 -= babyBoomerCount;
                else if (weightFor1 > babyBoomerCount)
                    weightFor1 -= babyBoomerCount;
            }
            weightFor3 = babyBoomerCount;

        } else {
            // Standard weighting for waves 4-9 (mostly)
            weightFor0 = (int) (enemyCount * 0.45f);
            weightFor1 = (int) (enemyCount * 0.35f);
            weightFor2 = enemyCount - weightFor0 - weightFor1;
            weightFor3 = 0;
        }

        for (int i = 0; i < weightFor0; i++)
            weightedPatterns.add(0);
        for (int i = 0; i < weightFor1; i++)
            weightedPatterns.add(1);
        for (int i = 0; i < weightFor2; i++)
            weightedPatterns.add(2);
        for (int i = 0; i < weightFor3; i++)
            weightedPatterns.add(3);

        return weightedPatterns;
    }

    private LevelTheme determineTheme(int levelNumber) {
        // if (levelNumber % 15 == 0)
        // return LevelTheme.VOID_DARK;
        // if (levelNumber % 5 == 0)
        // return LevelTheme.NEBULA_BLUE;
        return LevelTheme.NEBULA_BLUE;
    }

    // Classe auxiliar para estatísticas do jogador
    private static class PlayerStats {
        final int ammunitions;
        final int kills;
        final int streak;
        final int consecutiveKills;

        PlayerStats(int ammunitions, int kills, int streak, int consecutiveKills) {
            this.ammunitions = ammunitions;
            this.kills = kills;
            this.streak = streak;
            this.consecutiveKills = consecutiveKills;
        }
    }
}