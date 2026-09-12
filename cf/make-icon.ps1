Add-Type -AssemblyName System.Drawing

$cs = @"
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

public static class LeanIcon
{
    public static void Render(int S, string path)
    {
        float f = S / 512f;
        using (var bmp = new Bitmap(S, S, PixelFormat.Format32bppArgb))
        using (var g = Graphics.FromImage(bmp))
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.PixelOffsetMode = PixelOffsetMode.HighQuality;
            g.Clear(Color.Transparent);

            Color bg  = Color.FromArgb(255, 14, 20, 32);
            Color bg2 = Color.FromArgb(255, 25, 38, 58);
            Color amber  = Color.FromArgb(255, 255, 176, 32);
            Color amberD = Color.FromArgb(255, 168, 106, 14);
            Color mint   = Color.FromArgb(255, 61, 220, 132);
            Color mintD  = Color.FromArgb(255, 26, 138, 82);

            int r = Math.Max(8, (int)(96 * f));
            using (var gp = RoundedPath(0, 0, S, S, r))
            using (var lg = new LinearGradientBrush(new Rectangle(0, 0, S, S), bg2, bg, LinearGradientMode.Vertical))
                g.FillPath(lg, gp);

            int bs = (int)(58 * f);
            int x0 = (int)(52 * f);
            int[] ys = { 88, 227, 366 };

            using (var b = new SolidBrush(amber))
            using (var p = new Pen(amberD, Math.Max(1f, 3 * f)))
            {
                foreach (int yy in ys)
                {
                    int y = (int)(yy * f);
                    g.FillRectangle(b, x0, y, bs, bs);
                    g.DrawLine(p, x0, y + bs, x0 + bs, y + bs);
                    g.DrawLine(p, x0 + bs, y, x0 + bs, y + bs);
                }
            }

            int tipX = (int)(216 * f);
            int ax = (int)((x0 + bs + 8) * f);
            int half = (int)(9 * f);
            using (var ab = new SolidBrush(Color.FromArgb(175, 255, 255, 255)))
            {
                foreach (int yy in ys)
                {
                    int cy = (int)((yy + bs / 2) * f);
                    g.FillPolygon(ab, new[]
                    {
                        new Point(ax, cy - half),
                        new Point(tipX, cy),
                        new Point(ax, cy + half)
                    });
                }
            }

            int bx = (int)(240 * f), by = (int)(104 * f);
            int bw = (int)(200 * f), bh = (int)(304 * f);
            int br = Math.Max(4, (int)(26 * f));
            using (var bgp = RoundedPath(bx, by, bw, bh, br))
            {
                using (var mb = new SolidBrush(mint)) g.FillPath(mb, bgp);
                using (var mp = new Pen(mintD, Math.Max(2f, 6 * f))) g.DrawPath(mp, bgp);
            }

            using (var slat = new Pen(Color.FromArgb(70, 12, 60, 40), Math.Max(1f, 3 * f)))
            {
                for (int k = 1; k <= 4; k++)
                {
                    int sy = by + (int)(bh * k / 5.0);
                    g.DrawLine(slat, bx + 14 * f, sy, bx + bw - 14 * f, sy);
                }
            }

            float cx = bx + bw / 2f;
            float cyy = by + bh / 2f;
            float hw = 46 * f;
            float hh = 44 * f;
            using (var ink = new Pen(Color.FromArgb(235, 9, 44, 28), Math.Max(2f, 11 * f)))
            {
                ink.StartCap = LineCap.Round;
                ink.EndCap = LineCap.Round;
                g.DrawLine(ink, cx - hw, cyy - hh * 0.42f, cx + hw, cyy - hh * 0.42f);
                g.DrawLine(ink, cx - hw, cyy + hh * 0.42f, cx + hw, cyy + hh * 0.42f);
                float sk = 15 * f;
                g.DrawLine(ink, cx - sk - 4 * f, cyy - hh, cx - sk + 10 * f, cyy + hh);
                g.DrawLine(ink, cx + sk - 10 * f, cyy - hh, cx + sk + 4 * f, cyy + hh);
            }

            bmp.Save(path, ImageFormat.Png);
        }
    }

    private static GraphicsPath RoundedPath(int x, int y, int w, int h, int r)
    {
        var gp = new GraphicsPath();
        gp.AddArc(x, y, r, r, 180, 90);
        gp.AddArc(x + w - r, y, r, r, 270, 90);
        gp.AddArc(x + w - r, y + h - r, r, r, 0, 90);
        gp.AddArc(x, y + h - r, r, r, 90, 90);
        gp.CloseFigure();
        return gp;
    }
}
"@

Add-Type -TypeDefinition $cs -ReferencedAssemblies System.Drawing

$outDir = "D:\mutan\IdeaProjects\LeanObject\cf"
New-Item -ItemType Directory -Path $outDir -Force | Out-Null
[LeanIcon]::Render(512, "$outDir\icon.png")
[LeanIcon]::Render(128, "$outDir\preview-128.png")
[LeanIcon]::Render(64,  "$outDir\preview-64.png")
Write-Output "OK"
Get-ChildItem "$outDir\*.png" | ForEach-Object { "  {0}  {1} bytes" -f $_.Name, $_.Length }
