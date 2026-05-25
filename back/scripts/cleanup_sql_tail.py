from pathlib import Path

path = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "smartdiary.sql"
lines = path.read_text(encoding="utf-8").splitlines()
out = []
past_user = False
for line in lines:
    if line.startswith("ame,"):
        past_user = True
        continue
    if past_user:
        if "SET FOREIGN_KEY_CHECKS" in line:
            out.append(line)
        continue
    out.append(line)
    if "10000003" in line and "light');" in line.replace(" ", ""):
        past_user = True

path.write_text("\n".join(out) + "\n", encoding="utf-8")
print(f"cleaned -> {len(out)} lines")
