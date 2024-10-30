import type { Metadata } from "next";
// import localFont from "next/font/local";
import { League_Spartan } from "next/font/google";
import "./globals.css";
import { Provider } from "./providers";
import Navbar from "./components/Navbar/Navbar";

// const geistSans = localFont({
//   src: "./fonts/GeistVF.woff",
//   variable: "--font-geist-sans",
//   weight: "100 900",
// });
// const geistMono = localFont({
//   src: "./fonts/GeistMonoVF.woff",
//   variable: "--font-geist-mono",
//   weight: "100 900",
// });

const leagueSpartan = League_Spartan({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Create Next App",
  description: "Generated by create next app",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <Provider>
        <body className={`${leagueSpartan.className} bg-[#D4EBEF]`}>
          <Navbar />
          <main className="flex flex-wrap flex-col items-center justify-between mx-auto w-full">
            {children}
          </main>
        </body>
      </Provider>
    </html>
  );
}
