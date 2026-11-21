import { world, system } from "@minecraft/server";

const RADIUS = 24;
const UPDATE_TICKS = 40;

world.afterEvents.playerSpawn.subscribe((event) => {
  if (!event.initialSpawn) return;
  event.player.sendMessage("§b[HYFLEZ] §aBridge conectado neste mundo!");
  event.player.sendMessage("§7Monitor local de entidades ativo. Raio: " + RADIUS + " blocos.");
});

system.runInterval(() => {
  for (const player of world.getPlayers()) {
    try {
      const nearby = player.dimension.getEntities({
        location: player.location,
        maxDistance: RADIUS
      }).filter((entity) => entity.typeId !== "minecraft:player");

      let nearest = null;
      let nearestDistance = Infinity;

      for (const entity of nearby) {
        const dx = entity.location.x - player.location.x;
        const dy = entity.location.y - player.location.y;
        const dz = entity.location.z - player.location.z;
        const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < nearestDistance) {
          nearestDistance = distance;
          nearest = entity;
        }
      }

      const nearestLabel = nearest
        ? (nearest.nameTag || nearest.typeId.replace("minecraft:", "")) +
          " • " + nearestDistance.toFixed(1) + "m"
        : "Nenhuma entidade próxima";

      player.onScreenDisplay.setActionBar(
        "§bHYFLEZ BRIDGE §8| §fEntidades: §a" + nearby.length +
        " §8| §fMais próxima: §e" + nearestLabel
      );
    } catch (error) {
      player.sendMessage("§c[HYFLEZ] Erro ao atualizar o monitor: " + error);
    }
  }
}, UPDATE_TICKS);
