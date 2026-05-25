# -*- coding: utf-8 -*-
"""Remove oversized user INSERT lines and replace with compact seed data."""
from pathlib import Path

path = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "smartdiary.sql"
text = path.read_text(encoding="utf-8", errors="replace")
lines = text.splitlines(keepends=True)

out = []
i = 0
marker = "INSERT INTO `user` VALUES"
seed = """-- 演示用户（密码均为 123456 的 MD5）
INSERT INTO `user` (`id`, `uid`, `name`, `password`, `phone`, `email`, `allow_phone_search`, `hide_phone`, `hide_email`, `birthday`, `address`, `avatar`, `theme`) VALUES
(2, 10000000, 'hh', 'e10adc3949ba59abbe56e057f20f883e', '15548157447', NULL, 1, 0, 0, '2025-10-27', 'beijing', NULL, 'default'),
(3, 10000001, '123', 'e10adc3949ba59abbe56e057f20f883e', '15548157446', NULL, 1, 0, 0, '2005-01-18', '15,1509,150924', NULL, 'light'),
(4, 10000002, '夜梦水尚清', 'e10adc3949ba59abbe56e057f20f883e', '19935212852', NULL, 1, 0, 0, '2005-03-29', '14,1402,140213', NULL, 'default'),
(5, 10000003, 'sm', 'e10adc3949ba59abbe56e057f20f883e', '15598195164', NULL, 1, 0, 0, '2026-05-21', '14,1402,140213', NULL, 'light');
"""

inserted_seed = False
while i < len(lines):
    line = lines[i]
    stripped = line.lstrip()

    if stripped.startswith(marker):
        i += 1
        continue

    if "-- Records of user" in line:
        out.append(line)
        if not inserted_seed:
            out.append(seed + "\n")
            inserted_seed = True
        i += 1
        # skip duplicate comment lines from broken prior run
        while i < len(lines) and lines[i].strip().startswith("-- 演示用户"):
            i += 1
        while i < len(lines) and (
            lines[i].strip().startswith("INSERT INTO user")
            or lines[i].strip().startswith("INSERT INTO `user`")
            or (lines[i].strip().startswith("(") and "10000000" in lines[i])
        ):
            i += 1
        continue

    if stripped.startswith("INSERT INTO user") or (
        stripped.startswith("INSERT INTO `user`") and "VALUES" not in stripped
    ):
        i += 1
        while i < len(lines) and not lines[i].strip().endswith(");"):
            i += 1
        if i < len(lines):
            i += 1
        continue

    if stripped == "-- ----------------------------" and i + 1 < len(lines):
        nxt = lines[i + 1].strip()
        if nxt.startswith("INSERT INTO `user` VALUES"):
            i += 2
            continue

    out.append(line)
    i += 1

if not inserted_seed:
    # append before SET FOREIGN_KEY_CHECKS = 1
    final = []
    for line in out:
        if "SET FOREIGN_KEY_CHECKS = 1" in line and not inserted_seed:
            final.append(seed + "\n")
            inserted_seed = True
        final.append(line)
    out = final

# Remove corrupted tail from a previous broken PowerShell run
clean = []
done_user_seed = False
for line in out:
    if line.startswith("ame,") or line.startswith("\name,"):
        continue
    if done_user_seed:
        if "SET FOREIGN_KEY_CHECKS" in line:
            clean.append(line if line.endswith("\n") else line + "\n")
        continue
    clean.append(line)
    if "10000003" in line and "'light');" in line.replace(" ", ""):
        done_user_seed = True

path.write_text("".join(clean), encoding="utf-8", newline="\n")
print(f"Wrote {path}, lines={len(clean)}, size={path.stat().st_size}")
