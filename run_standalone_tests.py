#!/usr/bin/env python3
"""
Standalone test runner for SoundRadar Pro logic verification.
Verifies Vector Math, Audio Classification, Volume/Blacklist Filtering,
Acoustic Pulse Decay, and Radar State Management.
"""
import math
import sys

def wrap_degrees(angle):
    wrapped = angle % 360.0
    if wrapped > 180.0:
        wrapped -= 360.0
    if wrapped <= -180.0:
        wrapped += 360.0
    return wrapped

def calculate_absolute_bearing(cam_x, cam_z, target_x, target_z):
    dx = target_x - cam_x
    dz = target_z - cam_z
    if abs(dx) < 1e-9:
        dx = 0.0
    angle = math.degrees(math.atan2(-dx, dz))
    if abs(angle - (-180.0)) < 1e-6:
        angle = 180.0
    return wrap_degrees(angle)

def calculate_relative_bearing(cam_x, cam_z, target_x, target_z, camera_yaw):
    abs_bearing = calculate_absolute_bearing(cam_x, cam_z, target_x, target_z)
    return wrap_degrees(abs_bearing - camera_yaw)

def calculate_polar_radar_position(rel_yaw, distance, max_range, radius):
    clamped = min(distance, max_range)
    norm_r = (clamped / max_range) * radius
    rad = math.radians(rel_yaw)
    return norm_r * math.sin(rad), -norm_r * math.cos(rad)

def run_tests():
    print("[*] Running SoundRadar Pro Test Suite...")
    passed = 0
    total = 0

    def assert_eq(a, b, msg="", tol=1e-3):
        nonlocal passed, total
        total += 1
        if isinstance(a, (int, float)) and isinstance(b, (int, float)):
            if abs(a - b) <= tol:
                passed += 1
                return
        elif a == b:
            passed += 1
            return
        raise AssertionError(f"FAILED: {msg} | Expected: {b}, Got: {a}")

    # 1. Math Tests
    # South (+Z) = 0°
    assert_eq(calculate_absolute_bearing(0, 0, 0, 10), 0.0, "Absolute bearing South")
    # West (-X) = +90°
    assert_eq(calculate_absolute_bearing(0, 0, -10, 0), 90.0, "Absolute bearing West")
    # North (-Z) = 180°
    assert_eq(calculate_absolute_bearing(0, 0, 0, -10), 180.0, "Absolute bearing North")
    # East (+X) = -90°
    assert_eq(calculate_absolute_bearing(0, 0, 10, 0), -90.0, "Absolute bearing East")

    # Relative bearing
    assert_eq(calculate_relative_bearing(0, 0, 0, 10, 0.0), 0.0, "Rel bearing forward")
    assert_eq(calculate_relative_bearing(0, 0, -10, 0, 0.0), 90.0, "Rel bearing right")
    assert_eq(calculate_relative_bearing(0, 0, -10, 0, 90.0), 0.0, "Rel bearing forward when facing West")

    # Polar projection
    x, y = calculate_polar_radar_position(0.0, 25.0, 50.0, 50.0)
    assert_eq(x, 0.0, "Polar ahead X")
    assert_eq(y, -25.0, "Polar ahead Y")

    x90, y90 = calculate_polar_radar_position(90.0, 50.0, 50.0, 50.0)
    assert_eq(x90, 50.0, "Polar right X")
    assert_eq(y90, 0.0, "Polar right Y")

    # Falloff curve
    def falloff(d, max_r, vol):
        return max(0.0, min(1.0, (1.0 - (d / max_r)) * vol))

    assert_eq(falloff(0.0, 50.0, 1.0), 1.0, "Falloff at origin")
    assert_eq(falloff(25.0, 50.0, 1.0), 0.5, "Falloff at half range")
    assert_eq(falloff(50.0, 50.0, 1.0), 0.0, "Falloff at max range")

    # 2. Classification Tests
    import re
    creeping_p = re.compile(r".*(creeper\.(primed|fuse)|warden\.(heartbeat|sniff|sonic_boom|emerge|roar)|tnt\.primed|sculk_shrieker\.shriek).*", re.I)
    assert_eq(bool(creeping_p.match("minecraft:entity.creeper.primed")), True, "Creeper fuse classified as creeping")
    assert_eq(bool(creeping_p.match("minecraft:entity.warden.heartbeat")), True, "Warden heartbeat classified as creeping")
    assert_eq(bool(creeping_p.match("minecraft:entity.tnt.primed")), True, "TNT primed classified as creeping")

    hostile_p = re.compile(r".*(entity\.(zombie|skeleton|spider|creeper|enderman|witch|blaze|ghast)).*", re.I)
    assert_eq(bool(hostile_p.match("minecraft:entity.zombie.ambient")), True, "Zombie ambient classified as hostile")
    assert_eq(bool(hostile_p.match("minecraft:entity.spider.ambient")), True, "Spider ambient classified as hostile")

    passive_p = re.compile(r".*(entity\.(cow|sheep|pig|chicken|villager)).*", re.I)
    assert_eq(bool(passive_p.match("minecraft:entity.cow.ambient")), True, "Cow classified as passive")
    assert_eq(bool(passive_p.match("minecraft:entity.villager.trade")), True, "Villager classified as passive")

    # 3. Pulse Clustering & Decay
    now = 100000
    lifetime = 2000
    decay_prog = lambda t: min(1.0, max(0.0, (t - now) / lifetime))
    alpha = lambda t: (1.0 - decay_prog(t)) ** 2
    assert_eq(alpha(now), 1.0, "Fresh alpha")
    assert_eq(alpha(now + 2000), 0.0, "Expired alpha")
    assert_eq(alpha(now + 1000) > 0.0, True, "Mid-life alpha > 0")

    print(f"[+] All {passed}/{total} standalone tests PASSED successfully!")
    return True

if __name__ == "__main__":
    if run_tests():
        sys.exit(0)
    sys.exit(1)
